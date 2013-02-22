package org.improving

import MacroUtil._
import org.improving.InlinedList
import org.scalameter.api._


//object InlinedBenchmark extends PerformanceTest.Quickbenchmark {
//  val sizes = Gen.range("size")(300000, 15000000, 3000000)
//
//  val ranges = for {
//    size <- sizes
//  } yield 0 until size
//
////  performance of "InlinedList" in {
//  performance of "List" in {
//    measure method "map" in {
//      using(ranges) in {
//        r => r.map(_ + 1)
//      }
//    }
//  }
//}

object InlinedListBenchmark {
 def f1 = showUs(println(List(1, 2, 3) macroMap (_ * 10)))
//  def f1 = showUs(println(InlinedList(1, 2, 3) map (_ * 10)))
  
  def main(args: Array[String]): Unit = {
    f1
  }
}

//[info] ::Benchmark InlinedList.map::
//[info] cores: 8
//[info] jvm-name: Java HotSpot(TM) 64-Bit Server VM
//[info] jvm-vendor: Oracle Corporation
//[info] jvm-version: 23.7-b01
//[info] os-arch: amd64
//[info] os-name: Linux
//[info] Parameters(size -> 300000): 5.191286
//[info] Parameters(size -> 3300000): 46.142602
//[info] Parameters(size -> 6300000): 102.038292
//[info] Parameters(size -> 9300000): 250.473788
//[info] Parameters(size -> 12300000): 458.388883
//[info] 
//[info] Passed: : Total 0, Failed 0, Errors 0, Passed 0, Skipped 0
//[success] Total time: 143 s, completed Feb 14, 2013 12:38:11 AM
// VS.
//[info] ::Benchmark List.map::
//[info] cores: 8
//[info] jvm-name: Java HotSpot(TM) 64-Bit Server VM
//[info] jvm-vendor: Oracle Corporation
//[info] jvm-version: 23.7-b01
//[info] os-arch: amd64
//[info] os-name: Linux
//[info] Parameters(size -> 300000): 5.375216
//[info] Parameters(size -> 3300000): 47.719254
//[info] Parameters(size -> 6300000): 102.969885
//[info] Parameters(size -> 9300000): 277.030601
//[info] Parameters(size -> 12300000): 386.456689
