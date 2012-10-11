import scala.language.experimental.macros
import scala.reflect.macros.Context
import scala.collection.generic._
import scala.collection.mutable.WrappedArray

package improving {
  // TODO - implicit evidence for Coll relating it to CC[T]
  final class InfixMacroOpsImpl[T, Coll](val xs: Coll) extends InfixMacroOps[T, Coll] {
    def macroMap[U](f0: T => U): Coll = macro Impl.mapInfix[T, U, Coll, Coll]
    def macroForeach(f0: T => Unit): Unit = macro Impl.foreachInfix[T, Coll]
  }
}

package object improving {
  type ClassTag[A]      = scala.reflect.ClassTag[A]
  type Lin[+T]          = scala.collection.LinearSeq[T]
  type Ind[+T]          = scala.collection.IndexedSeq[T]
  type Ctx              = scala.reflect.macros.Context
  type CtxCC[T, CC[_]]  = Context { type PrefixType = InfixMacroOps[T, CC[T]] }
  type CtxColl[T, Coll] = Context { type PrefixType = InfixMacroOps[T, Coll] }

  implicit def mkArrayMacroOps[T](xs: Array[T]): InfixMacroOpsImpl[T, Array[T]] = new InfixMacroOpsImpl[T, Array[T]](xs)
  implicit def mkInfixMacroOps[T, Coll](xs: Coll with TraversableOnce[T]): InfixMacroOpsImpl[T, Coll] = new InfixMacroOpsImpl[T, Coll](xs)

  def numberThing(s: String): Int = try s.toDouble.toInt catch { case _: NumberFormatException => -1 }
  def ashow(xs: Array[_]) = println(xs mkString ", ")
}
