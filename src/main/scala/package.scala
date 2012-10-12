import scala.language.experimental.macros
import scala.reflect.macros.Context
import scala.collection.generic._
import scala.collection.mutable.WrappedArray

package improving {
  // TODO - implicit evidence for Coll relating it to CC[A]
  final class InfixMacroOpsImpl[A, Coll](val xs: Coll) extends InfixMacroOps[A, Coll] {
    def macroMap[B](f0: A => B): Coll = macro Impl.mapInfix[A, B, Coll, Coll]
    def macroForeach(f0: A => Unit): Unit = macro Impl.foreachInfix[A, Coll]
  }
}

package object improving {
  type ClassTag[A]      = scala.reflect.ClassTag[A]
  type Lin[+A]          = scala.collection.LinearSeq[A]
  type Ind[+A]          = scala.collection.IndexedSeq[A]
  type Ctx              = scala.reflect.macros.Context
  type CtxCC[A, CC[_]]  = Context { type PrefixType = InfixMacroOps[A, CC[A]] }
  type CtxColl[A, Coll] = Context { type PrefixType = InfixMacroOps[A, Coll] }

  implicit def mkArrayMacroOps[A](xs: Array[A]): InfixMacroOpsImpl[A, Array[A]] = new InfixMacroOpsImpl[A, Array[A]](xs)
  implicit def mkInfixMacroOps[A, Coll](xs: Coll with TraversableOnce[A]): InfixMacroOpsImpl[A, Coll] = new InfixMacroOpsImpl[A, Coll](xs)

  def numberThing(s: String): Int = try s.toDouble.toInt catch { case _: NumberFormatException => -1 }
  def ashow(xs: Array[_]) = println(xs mkString ", ")
}
