package improving

// TODO: constraints on Coll
trait InfixMacroOps[A, Coll] extends Any {
  def xs: Coll
}

// TODO - implicit evidence for Coll relating it to CC[A]
@macroExtension final class InfixMacroOpsImpl[A, Coll](val xs: Coll) extends InfixMacroOps[A, Coll] {
  def macroMap[B](f0: A => B): Coll     = macro Declosurify.mapInfix[A, B, Coll, Coll]
  def macroForeach(f0: A => Unit): Unit = macro Declosurify.foreachInfix[A, Coll]
}
