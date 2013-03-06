package org.improving.sample.collection

import generic._
//import mutable.Builder

/** A base trait for linear sequences.
 *  $linearSeqInfo
 */
trait LinearSeq[+A] { // extends Seq[A] {
//  override def companion: GenericCompanion[LinearSeq] = LinearSeq
  def seq: LinearSeq[A] = this
}


//object LinearSeq extends GenericCompanion[LinearSeq] {
//  implicit def canBuildFrom[A]: CanBuildFrom[Coll, A, LinearSeq[A]] = ReusableCBF.asInstanceOf[GenericCanBuildFrom[A]]
//  def newBuilder[A]: Builder[A, LinearSeq[A]] = immutable.LinearSeq.newBuilder[A]
//}
