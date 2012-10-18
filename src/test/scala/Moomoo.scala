import improving._
import MacroUtil._
import scala.collection.{ mutable, immutable, generic }

object MooMoo {
  def a1 = showUs(println(Array(1, 2, 3) macroMap (_ * 10) mkString ("Array(", ", ", ")")))
  def a2 = showUs({ Array(1, 2, 3) macroForeach (x => print((x * 10) + " ")); println("") })
  def f1 = showUs(println(List(1, 2, 3) macroMap (_ * 10)))
  def f2 = showUs({ List(1, 2, 3) macroForeach (x => print((x * 10) + " ")); println("") })
  def g1 = showUs(println(immutable.Vector(1, 2, 3) macroMap (_ * 10)))
  def g2 = showUs({ immutable.Vector(1, 2, 3) macroForeach (x => print((x * 10) + " ")); println("") })
  def h1 = showUs(println(immutable.Traversable(1, 2, 3) macroMap (_ * 10)))
  def h2 = showUs({ immutable.Traversable(1, 2, 3) macroForeach (x => print((x * 10) + " ")); println("") })
  // TODO
  def r1 = showUs(println(1 to 3 macroMap (_ * 10)))
  def r2 = showUs({ 1 to 3 macroForeach (x => print((x * 10) + " ")); println("") })

  def main(args: Array[String]): Unit = {
    a1 ; a2
    f1 ; f2
    g1 ; g2
    h1 ; h2

    Moo.flatCollect(List(1,2,3))
  }
}

object Moo {
  def flatCollect(elems: List[Int]): Unit = {
    showUs(elems macroForeach println)
    showUs(elems macroForeach (x => println(x)))
  }
}
