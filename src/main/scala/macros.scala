package improving

import scala.language.experimental.macros
import scala.reflect.macros.Context
import scala.collection.TraversableLike
import scala.collection.generic._
import scala.collection.mutable.Builder
import scala.reflect.api.Universe
import scala.reflect.NameTransformer.{ encode => encodeName }

trait ReflectionSupport {
  val u: Universe
  import u._

  // Smuggled from the compiler.
  val METHOD = (1L << 6).asInstanceOf[FlagSet]

  def mkUnit = Literal(Constant(()))
  def mkApply(x: scala.Symbol)(args: Tree*): Apply = Apply(x, args.toList)
  def flatten(t: Tree): List[Tree] = t match {
    case Block(xs, expr) => xs :+ expr
    case _               => t :: Nil
  }

  implicit final class TypeOps(val tp: Type) {
    def typeArgs = tp match {
      case TypeRef(_, _, args) => args
      case _                   => Nil
    }
    def isLinearSeqType   = tp <:< typeOf[Lin[_]]
    def isIndexedSeqType  = tp <:< typeOf[Ind[_]]
    def isTraversableType = tp <:< typeOf[Traversable[_]]
    def isArrayType       = tp <:< typeOf[Array[_]]

    def orElse(alt: => Type): Type = if (tp eq NoType) alt else tp
  }

  implicit def optionTypeIsNoType(tp: Option[Type]): Type    = tp getOrElse NoType
  implicit def optionSymIsNoSym(sym: Option[Symbol]): Symbol = sym getOrElse NoSymbol
  implicit def symbolToTermName(x: scala.Symbol): TermName   = newTermName(encodeName(x.name))
  implicit def symbolToIdent(x: scala.Symbol): Ident         = Ident(x: TermName)
}

trait MacroSupport extends ReflectionSupport {
  val c: Context
  import c.universe._

  def dumpContext(): Unit = println(
    s"""
      macroApplication   =${c.macroApplication}
      enclosingMacros    =${c.enclosingMacros}
      enclosingImplicits =${c.enclosingImplicits}
      enclosingPosition  =${c.enclosingPosition}
      enclosingMethod    =${c.enclosingMethod}
      enclosingClass     =${c.enclosingClass}
      enclosingUnit      =${c.enclosingUnit}
      enclosingRun       =${c.enclosingRun}
    """
  )

  def freshName(prefix: String): TermName = newTermName(c.fresh(prefix))
  def enclClass = c.enclosingClass.symbol
  def enclMethod = c.enclosingMethod match {
    case null   =>
      c.warning(c.enclosingPosition, s"enclosingMethod is null, using $enclClass instead.")
      dumpContext()
      enclClass
    case m      => m.symbol
  }
}

class ContextUtil[C <: Context](final val c: C) extends ReflectionSupport with MacroSupport {
  val u: c.universe.type = c.universe
  import u._

  private def isMacroOpsImplicit(m: Symbol) = (
       m.fullName == "improving.mkArrayMacroOps"
    || m.fullName == "improving.mkInfixMacroOps"
  )

  lazy val collectionType: Type = Some(c.prefix.actualType.typeArgs) collect { case _ :: coll :: Nil => coll }
  lazy val elementType: Type    = Some(c.prefix.actualType.typeArgs) collect { case elem :: _ :: Nil => elem }

  object ArrayPrefix {
    def unapply[T](prefix: c.Expr[T]): Option[Tree] =
      if (collectionType.isArrayType) Some(prefixCollectionTree) else None
  }
  object LinearPrefix {
    def unapply[T](prefix: c.Expr[T]): Option[Tree] =
      if (collectionType.isLinearSeqType) Some(prefixCollectionTree) else None
  }
  object IndexedPrefix {
    def unapply[T](prefix: c.Expr[T]): Option[Tree] =
      if (collectionType.isIndexedSeqType) Some(prefixCollectionTree) else None
  }
  object TraversablePrefix {
    def unapply[T](prefix: c.Expr[T]): Option[Tree] =
      if (collectionType.isTraversableType) Some(Select(prefixCollectionTree, 'toIterator)) else None
  }

  def prefixCollection[Coll1] = c.Expr[Coll1](prefixCollectionTree)

  def prefixCollectionTree = {
    c.prefix.tree match {
      case Apply(sel, arg :: Nil) if isMacroOpsImplicit(sel.symbol) => arg
      case _                                                        => Apply(c.prefix.tree, Ident('xs) :: Nil)
    }
  }
  implicit def scalaSymbolToInvokeOps(x: scala.Symbol): InvokeOps = treeToInvokeOps(Ident(x.name: TermName))
  implicit def reflectSymbolToInvokeOps(x: Symbol): InvokeOps     = treeToInvokeOps(Ident(x))
  implicit def treeToInvokeOps(x: Tree): InvokeOps                = new InvokeOps(x)
  implicit def lowerInvokeOps(ops: InvokeOps): Tree               = ops.lhs

  class InvokeOps(val lhs: Tree) {
    def dot(name: scala.Symbol): InvokeOps = dot(name: TermName)
    def dot(name: Name): InvokeOps         = new InvokeOps(Select(lhs, name))
    def apply(args: Tree*): Tree           = Apply(lhs, args.toList)
    def apply[T1: c.WeakTypeTag] : Tree    = TypeApply(lhs, List(weakTypeOf[T1]) map (t => TypeTree(t)))
  }

  // Just to verify I needed the METHOD flag, without it I get:
  //
  // [error] scala.ScalaReflectionException: value local1 is not a method
  // [error]   at scala.reflect.api.Symbols$SymbolApi$class.asMethod(Symbols.scala:151)
  // [error]   at scala.reflect.internal.Symbols$SymbolContextApiImpl.asMethod(Symbols.scala:73)
  // [error]   at improving.ContextUtil.newLocalMethod(macros.scala:65)
  // [error]   at improving.ContextUtil.functionToLocalMethod(macros.scala:73)
  // [error]   at improving.ContextUtil.functionToLocalMethod(macros.scala:80)
  // [error]   at improving.Impl$.amapImpl(macros.scala:187)
  // [error]   at improving.Impl$.amapInfix(macros.scala:151)
  def newLocalMethod(name: TermName, paramTypes: List[Type], resultType: Type): MethodSymbol = {
    import build._
    val ddef       = newNestedSymbol(enclMethod, name, enclMethod.pos, METHOD, isClass = false)
    val params     = paramTypes map (tpe =>
      setTypeSignature(
        newNestedSymbol(ddef, freshName("p"), ddef.pos, Flag.PARAM, isClass = false),
        tpe
      )
    )
    setTypeSignature(ddef, MethodType(params, resultType)).asMethod
  }

  def functionToLocalMethod(fnTree: Function): DefDef = {
    val Function(fparams, fbody) = fnTree
    val fsyms      = fparams map (_.symbol)
    val ftypes     = fparams map (_.symbol.typeSignatureIn(c.enclosingClass.symbol.typeSignature))
    val resultType = fbody.tpe
    val ddef       = newLocalMethod(freshName("local"), ftypes, resultType)
    val fbody1     = fbody.substituteSymbols(fsyms, ddef.paramss.head)

    c.typeCheck(DefDef(ddef, fbody1)).asInstanceOf[DefDef]
  }

  def functionToLocalMethod(tree: Tree): DefDef = flatten(tree) match {
    case (f: Function) :: Nil => functionToLocalMethod(f)
    case _                    => c.abort(c.enclosingPosition, s"Cannot find closure in $tree")
  }
}

object Impl {
  def mapInfix[T: c0.WeakTypeTag, U: c0.WeakTypeTag, Coll: c0.WeakTypeTag, That: c0.WeakTypeTag](c0: CtxColl[T, Coll])(f0: c0.Expr[T => U]): c0.Expr[That] = {
    val ctx = new ContextUtil[c0.type](c0)
    import ctx._
    import c.universe._

    val closureTree = functionToLocalMethod(f0.tree)
    def closure     = closureTree.symbol

    def isForeach         = weakTypeOf[That] =:= typeOf[Unit]
    def newBuilder        = weakTypeOf[Coll].typeSymbol.companionSymbol.typeSignature member 'newBuilder
    def closureDef        = c.Expr[Unit](closureTree)
    def builderVal        = c.Expr[Unit](if (isForeach) mkUnit else ValDef(NoMods, 'buf, TypeTree(), newBuilder[U]))
    def mkCall(arg: Tree) = c.Expr[Unit](if (isForeach) closure(arg) else ('buf dot '+=)(closure(arg)))
    def mkResult          = c.Expr[That](if (isForeach) mkUnit else 'buf dot 'result)

    def mkIndexed[Prefix](prefixTree: Tree): c.Expr[That] = {
      val prefix = c.Expr[Prefix](prefixTree)
      val len    = c.Expr[Int]('xs dot 'length) // might be array or indexedseq
      val call   = mkCall('xs('i))

      reify {
        closureDef.splice
        builderVal.splice
        val xs = prefix.splice
        var i  = 0
        while (i < len.splice) {
          call.splice
          i += 1
        }
        mkResult.splice
      }
    }

    def mkLinear(prefixTree: Tree): c.Expr[That] = {
      val prefix    = c.Expr[Lin[T]](prefixTree)
      val call = mkCall('these dot 'head)

      reify {
        closureDef.splice
        builderVal.splice
        var these = prefix.splice
        while (!these.isEmpty) {
          call.splice
          these = these.tail
        }
        mkResult.splice
      }
    }

    def mkTraversable(prefixTree: Tree): c.Expr[That] = {
      val prefix = c.Expr[Traversable[T]](prefixTree)
      val call   = mkCall('it dot 'next)

      reify {
        closureDef.splice
        builderVal.splice
        val it = prefix.splice.toIterator
        while (it.hasNext)
          call.splice

        mkResult.splice
      }
    }

    c.prefix match {
      case ArrayPrefix(tree)       => mkIndexed[Array[T]](tree)
      case IndexedPrefix(tree)     => mkIndexed[Ind[T]](tree)
      case LinearPrefix(tree)      => mkLinear(tree)
      case TraversablePrefix(tree) => mkTraversable(tree)
      case _                       => c.abort(c.enclosingPosition, "Not a Traversable: " + collectionType)
    }
  }

  def foreachInfix[T: c0.WeakTypeTag, Coll: c0.WeakTypeTag](c0: CtxColl[T, Coll])(f0: c0.Expr[T => Unit]): c0.Expr[Unit] =
    mapInfix[T, Unit, Coll, Unit](c0)(f0)
}

object MacroUtil {
  def showUs[T](a: T): T = macro showUsImpl[T]
  def showUsImpl[T](c: Context)(a: c.Expr[T]) = {
    Console.err.println(c.universe.show(a.tree))
    a
  }
}
