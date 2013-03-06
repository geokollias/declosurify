package org.improving

import MacroUtil._
import org.scalameter.api._
import org.scalameter.execution
import org.scalameter.reporting


//object InlinedBenchmark extends PerformanceTest.Quickbenchmark {
//object InlinedBenchmark extends PerformanceTest.Microbenchmark {
object InlinedBenchmark extends PerformanceTest {
  import Executor.Measurer
  def warmer = Executor.Warmer.Default()
  def aggregator = Aggregator.complete(Aggregator.average)
  def measurer = new Measurer.IgnoringGC with Measurer.PeriodicReinstantiation with Measurer.OutlierElimination with Measurer.RelativeNoise
  def executor = execution.SeparateJvmsExecutor(warmer, aggregator, measurer)
  def reporter = new reporting.LoggingReporter
  def persistor = Persistor.None
  
  val sizes = Gen.range("size")(1000000, 3000000, 500000)
//  val sizes = Gen.range("size")(1, 10, 2)
  val lists = for (sz <- sizes) yield (0 until sz).toList

  performance of "List" config (
      exec.reinstantiation.frequency -> 2,
      exec.reinstantiation.fullGC -> true,
      exec.benchRuns -> 36,
      exec.independentSamples -> 6
    ) in {
//    measure method "macroMap" in { // test group
//      using(lists) in {
//        def f(r: List[Int]) = r.macroMap(_ + 1)
//        r => f(r) // curve
//      }
//    }
    measure method "map" in {
      using(lists) in {
        def f(r: List[Int]) = r.map(_ + 1)
        r => f(r)
//        r => r.map(_ + 1)
      }
    }
  }
}

//object InlinedListBenchmark {
//  def f1 = showUs(println(List(1, 2, 3) macroMap (_ * 10)))
////  def f1 = showUs(println(InlinedList(1, 2, 3) map ((x: Int) => x * 10)))
//
//  def main(args: Array[String]): Unit = {
//    f1
//  }
//}

//[info] ::Benchmark List.macroMap::
//[info] cores: 8
//[info] hostname: geokollias-trbx
//[info] jvm-name: Java HotSpot(TM) 64-Bit Server VM
//[info] jvm-vendor: Oracle Corporation
//[info] jvm-version: 23.7-b01
//[info] os-arch: amd64
//[info] os-name: Linux
//[info] Parameters(size -> 1000000): 16.09330608333333
//[info] Parameters(size -> 1500000): 28.70362186111111
//[info] Parameters(size -> 2000000): 30.522613888888884
//[info] Parameters(size -> 2500000): 35.884936972222214
//[info] Parameters(size -> 3000000): 43.65284405555556
//[info] 
//[info] ::Benchmark List.map::
//[info] cores: 8
//[info] hostname: geokollias-trbx
//[info] jvm-name: Java HotSpot(TM) 64-Bit Server VM
//[info] jvm-vendor: Oracle Corporation
//[info] jvm-version: 23.7-b01
//[info] os-arch: amd64
//[info] os-name: Linux
//[info] Parameters(size -> 1000000): 24.625723083333334
//[info] Parameters(size -> 1500000): 36.49562130555555
//[info] Parameters(size -> 2000000): 46.6049605
//[info] Parameters(size -> 2500000): 54.616660444444435
//[info] Parameters(size -> 3000000): 68.63785094444444
//[info] 
//[info] Passed: : Total 0, Failed 0, Errors 0, Passed 0, Skipped 0
//[success] Total time: 505 s, completed Feb 28, 2013 9:13:23 PM

