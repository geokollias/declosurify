package org.improving

trait ReflectionSupport {
  val u: Universe
  import u._

  // Smuggled from the compiler.
  val METHOD = (1L << 6).asInstanceOf[FlagSet]

  def weakTypesOf[T1: WeakTypeTag] : List[Type]                                                    = List(weakTypeOf[T1])
  def weakTypesOf[T1: WeakTypeTag, T2: WeakTypeTag] : List[Type]                                   = List(weakTypeOf[T1], weakTypeOf[T2])
  def weakTypesOf[T1: WeakTypeTag, T2: WeakTypeTag, T3: WeakTypeTag] : List[Type]                  = List(weakTypeOf[T1], weakTypeOf[T2], weakTypeOf[T3])
  def weakTypesOf[T1: WeakTypeTag, T2: WeakTypeTag, T3: WeakTypeTag, T4: WeakTypeTag] : List[Type] = List(weakTypeOf[T1], weakTypeOf[T2], weakTypeOf[T3], weakTypeOf[T4])

  def typesOf[T1: TypeTag, T2: TypeTag] : List[Type]                           = List(typeOf[T1], typeOf[T2])
  def typesOf[T1: TypeTag, T2: TypeTag, T3: TypeTag] : List[Type]              = List(typeOf[T1], typeOf[T2], typeOf[T3])
  def typesOf[T1: TypeTag, T2: TypeTag, T3: TypeTag, T4: TypeTag] : List[Type] = List(typeOf[T1], typeOf[T2], typeOf[T3], typeOf[T4])

  def symbolOf[T: TypeTag] = typeOf[T].typeSymbol
  def mkUnit = Literal(Constant(()))
  def mkApply(x: scala.Symbol)(args: Tree*): Apply = Apply(x, args.toList)
  def flatten(t: Tree): List[Tree] = t match {
    case Block(xs, expr) => xs :+ expr
    case _               => t :: Nil
  }
  def methPart(tree: Tree): Tree = tree match {
    case Apply(fn, _)           => methPart(fn)
    case TypeApply(fn, _)       => methPart(fn)
    case AppliedTypeTree(fn, _) => methPart(fn)
    case _                      => tree
  }

  implicit final class TypeOps(val tp: Type) {
    def typeArgs = tp match {
      case TypeRef(_, _, args) => args
      case _                   => Nil
    }
    def finalResultType: Type = tp match {
      case NullaryMethodType(restpe) => restpe.finalResultType
      case MethodType(_, restpe)     => restpe.finalResultType
      case PolyType(_, restpe)       => restpe.finalResultType
      case tp                        => tp
    }

    def definitionAnnotations = tp.typeSymbol.annotations
    def useAnnotations = tp match {
      case AnnotatedType(annotations, _, _) => annotations
      case _                                => Nil
    }

    def isDefinedWithAnnotation[T <: ScalaAnnotation : TypeTag] = definitionAnnotations exists (_.tpe =:= typeOf[T])
    def isUsedWithAnnotation[T <: ScalaAnnotation : TypeTag]    = useAnnotations exists (_.tpe =:= typeOf[T])

    def isLinearSeqType   = tp <:< typeOf[Lin[_]]
    def isIndexedSeqType  = tp <:< typeOf[Ind[_]]
    def isTraversableType = tp <:< typeOf[Traversable[_]]
    def isArrayType       = tp <:< typeOf[Array[_]]

    def orElse(alt: => Type): Type = if (tp eq NoType) alt else tp
  }

  implicit final class SymbolOps(val sym: Symbol) {
    def ownerChain_s = ownerChain takeWhile (sym => !sym.isPackageClass) mkString " -> "
    def ownerChain: List[Symbol] = sym match {
      case NoSymbol => Nil
      case _        => sym :: sym.owner.ownerChain
    }
    def firstParam = sym.asMethod.paramss.flatten match {
      case Nil    => NoSymbol
      case p :: _ => p
    }
    def producedType: Type = sym match {
      case m: MethodSymbol => m.typeSignature.finalResultType
      case _               => NoType
    }
  }

  implicit def optionTypeIsNoType(tp: Option[Type]): Type    = tp getOrElse NoType
  implicit def optionSymIsNoSym(sym: Option[Symbol]): Symbol = sym getOrElse NoSymbol
  implicit def symbolToTermName(x: scala.Symbol): TermName   = newTermName(encodeName(x.name))
  implicit def symbolToIdent(x: scala.Symbol): Ident         = Ident(x: TermName)
}
