package org.improving

//import scala.collection.immutable.LinearSeq


//@macroExtension
//class InlinedList[+A](val xs: List[A]) extends LinearSeq[A] { // since we extend LinearSeq check if the "object InlinedList" is needed..
//  override def toList: List[A] = xs
//  def map[B](f0: A => B): InlinedList[B] = macro Declosurify.mapInfix[A, B, InlinedList[A], InlinedList[B]]
//  override def toString = xs.toString
//  override def length = xs.length
//  override def apply(n: Int) = xs.apply(n)
//}
//
//object InlinedList {
//  def apply[A](xs: A*): InlinedList[A] = new InlinedList(xs.toList)
//  implicit def inlined2plainList[A](xs: InlinedList[A]): List[A] = xs.toList
//  implicit def plain2inlinedList[A](xs: List[A]): InlinedList[A] = new InlinedList(xs)
//}

//object InlinedListTest {
//  implicit class InlinedList[A](val l: List[A]) {
//    def map1[B](f0: A => B): List[B] = macro Declosurify.mapInfix[A, B, List[A], List[B]]
//    def map[B](f0: A => B): List[B] = {
//      println("InlinedList::map(" + f0 + ")")
//      l.map(f0)
//    }
//  }
//  def fmap1 = List(1,2,3).map1(_ + 1)
//}


object InlinedListTest {
  import org.improving.sample.collection.immutable.List
  
  def fmap1 = List(1,2,3).map1(_ + 1)
}