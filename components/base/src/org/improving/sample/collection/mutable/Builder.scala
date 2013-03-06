package org.improving.sample.collection.mutable

import org.improving.sample.collection.generic.Growable
import org.improving.sample.collection.TraversableLike
import org.improving.sample.collection.immutable._
import org.improving.sample.collection.generic.CanBuildFrom

trait Builder[-Elem, +To] extends Growable[Elem] {
  def +=(elem: Elem): this.type
  def clear()
  def result(): To
  def sizeHint(size: Int) {}
}


