package org.improving.sample.collection.generic
import org.improving.sample.collection.immutable._

trait Shrinkable[-A] {
  def --(): this.type
  def -=(elem: A): this.type = --
}




