package org.improving.sample.collection.mutable

trait HashEntry [A, E] {
  val key: A
  var next: E = _
}
