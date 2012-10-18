import scala.reflect.macros.Context
import scala.collection.generic._
import scala.collection.mutable.WrappedArray

package object improving {
  implicit lazy val macrosFeature = scala.language.experimental.macros

  type ScalaAnnotation  = scala.annotation.Annotation
  type Universe         = scala.reflect.api.Universe
  type ClassTag[A]      = scala.reflect.ClassTag[A]
  type Lin[+A]          = scala.collection.LinearSeq[A]
  type Ind[+A]          = scala.collection.IndexedSeq[A]
  type Ctx              = scala.reflect.macros.Context
  type CtxCC[A, CC[_]]  = Context { type PrefixType = InfixMacroOps[A, CC[A]] }
  type CtxColl[A, Coll] = Context { type PrefixType = InfixMacroOps[A, Coll] }

  implicit def mkArrayMacroOps[A](xs: Array[A]): InfixMacroOpsImpl[A, Array[A]] = new InfixMacroOpsImpl[A, Array[A]](xs)
  implicit def mkInfixMacroOps[A, Coll](xs: Coll with TraversableOnce[A]): InfixMacroOpsImpl[A, Coll] = new InfixMacroOpsImpl[A, Coll](xs)

  def encodeName(name: String) = scala.reflect.NameTransformer.encode(name)
  def decodeName(name: String) = scala.reflect.NameTransformer.decode(name)

  def numberThing(s: String): Int = try s.toDouble.toInt catch { case _: NumberFormatException => -1 }
  def ashow(xs: Array[_]) = println(xs mkString ", ")
}
