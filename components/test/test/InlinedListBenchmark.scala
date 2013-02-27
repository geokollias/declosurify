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
  
  val sizes = Gen.range("size")(1000000, 2000000, 500000)
  val lists = for (sz <- sizes) yield (0 until sz).toList

  performance of "List" config (
      exec.reinstantiation.frequency -> 2,
      exec.reinstantiation.fullGC -> true,
      exec.benchRuns -> 36,
      exec.independentSamples -> 6
    ) in {
    measure method "map" in {
      using(lists) in {
        r => r.map(_ + 1)
      }
    }
    measure method "macroMap" in { // test group
      using(lists) in {
        r => r.macroMap(_ + 1) // curve
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

//::Benchmark List.macroMap::
//Parameters(size -> 1000000): 9.907533
//Parameters(size -> 1500000): 16.174634
//Parameters(size -> 2000000): 21.244165
//Parameters(size -> 2500000): 26.108901
//
//::Benchmark List.map::
//Parameters(size -> 1000000): 11.179443
//Parameters(size -> 1500000): 20.099707
//Parameters(size -> 2000000): 23.935674
//Parameters(size -> 2500000): 31.500315

