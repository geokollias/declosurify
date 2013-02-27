package org.improving

import scala.collection.immutable.LinearSeq


@macroExtension
class InlinedList[+A](val xs: List[A]) extends LinearSeq[A] { // since we extend LinearSeq check if the "object InlinedList" is needed..
  override def toList: List[A] = xs
  def map[B](f0: A => B): InlinedList[B] = macro Declosurify.mapInfix[A, B, InlinedList[A], InlinedList[B]]
  override def toString = xs.toString
  override def length = xs.length
  override def apply(n: Int) = xs.apply(n)
}

object InlinedList {
  def apply[A](xs: A*): InlinedList[A] = new InlinedList(xs.toList)
  implicit def inlined2plainList[A](xs: InlinedList[A]): List[A] = xs.toList
  implicit def plain2inlinedList[A](xs: List[A]): InlinedList[A] = new InlinedList(xs)
}
