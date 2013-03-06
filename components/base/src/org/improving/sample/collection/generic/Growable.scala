
package org.improving.sample.collection.generic

import org.improving.sample.collection.immutable._

trait Growable[-A] {
  def +=(elem: A): this.type
  def ++=(xs: List[A]): this.type = { xs foreach += ; this }
  def clear()
}
