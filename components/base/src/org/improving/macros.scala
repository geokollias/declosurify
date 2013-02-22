package org.improving

import scala.collection.generic.FilterMonadic
import scala.languageFeature.experimental.macros

class MacroSupport[C <: Ctx](final val c: C) extends ReflectionSupport {
  val u: c.universe.type = c.universe
  import u._

  def dumpContext(): Unit = println(
    s"""
      macroApplication   =${c.macroApplication}
      enclosingMacros    =${c.enclosingMacros}
      enclosingImplicits =${c.enclosingImplicits}
      enclosingPosition  =${c.enclosingPosition}
      enclosingMethod    =${c.enclosingMethod}
      enclosingClass     =${c.enclosingClass}
      enclosingUnit      =${c.enclosingUnit}
      enclosingRun       =${/*c.enclosingRun*/ "run" }
    """
  )

  def freshName(prefix: String): TermName = newTermName(c.fresh(prefix))
  def enclClass: ClassSymbol = c.enclosingClass.symbol match {
    case x: ModuleSymbol => x.moduleClass.asClass
    case x: ClassSymbol  => x
  }
  def enclMethod = c.enclosingMethod match {
    case null   =>
      c.warning(c.enclosingPosition, s"enclosingMethod is null, using $enclClass instead.")
      dumpContext()
      enclClass
    case m      => m.symbol
  }

  private def logging = sys.props contains "declosurify.debug"


  def log(msg: String) = if (logging) Console.err.println(msg)
  def log_?[T](value: T)(pf: PartialFunction[T, Any]): T = {
    if (pf isDefinedAt value)
      log("" + pf(value))

    value
  }

  private def isMacroOpsImplicit(method: Symbol) = log_?(
       method.producedType.isDefinedWithAnnotation[macroExtension]
    && (method.producedType member 'xs) != NoSymbol
  ) {
    case false =>
      ((method.producedType, method.producedType.isDefinedWithAnnotation[macroExtension], method.producedType member 'xs))
  }

  lazy val collectionType: Type = {
    System.err.println("collectionType: " + c.prefix.actualType + "   " + c.prefix.actualType.typeArgs)
    //Some(c.prefix.actualType) <= this triggers changes to InlinedList but misses cbf info
    Some(c.prefix.actualType.typeArgs) collect { case _ :: coll :: Nil => coll }
  }
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
    val m = methPart(c.prefix.tree)
    println(s"""
        |   pre: $m
        |actual: ${c.prefix.actualType}
        |static: ${c.prefix.staticType}
        | first: ${m.tpe}
      """.stripMargin.trim
    )
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
  def newLocalMethod(name: TermName, vparams: List[ValDef], resultType: Type): MethodSymbol = {
    import build._
    val ddef = newNestedSymbol(enclMethod, name, enclMethod.pos, Flag.PRIVATE | METHOD, isClass = false)
    val params = vparams map { vd =>
      val sym = newNestedSymbol(ddef, vd.name, ddef.pos, Flag.PARAM, isClass = false)
      setTypeSignature(sym, vd.tpt.tpe)
      vd setSymbol sym
      sym
    }
    setTypeSignature(ddef, MethodType(params, resultType)).asMethod
  }

  def functionToLocalMethod(fnTree: Function): DefDef = {
    val Function(fparams, fbody) = fnTree
    val frestpe = fbody.tpe
    val fsyms   = fparams map (_.symbol)
    val vparams = for (vd @ ValDef(mods, name, tpt, _) <- fparams) yield ValDef(mods, name, TypeTree(vd.symbol.typeSignature.normalize), EmptyTree)
    val method  = newLocalMethod(freshName("local"), vparams, frestpe)
    val tree    = DefDef(NoMods, freshName("local"), Nil, List(vparams), TypeTree(frestpe), c.resetAllAttrs(fbody.duplicate))

    tree setSymbol method
    c.resetAllAttrs(tree)
    c.typeCheck(tree).asInstanceOf[DefDef]
  }
}

object MacroUtil {
  def showUs[T](a: T): T = macro showUsImpl[T]
  def showUsImpl[T](c: Ctx)(a: c.Expr[T]) = {
    System.err.println(c.universe.show(a.tree))
    a
  }
}
