package com.matthalstead.workdispatcher.benchmarks

import com.matthalstead.workdispatcher.ElasticThreadPoolWorkDispatcher
import com.matthalstead.workdispatcher.SeparateThreadPoolsWorkDispatcher
import com.matthalstead.workdispatcher.WorkDispatcher

class RunBenchmark(
    val workDispatcher: WorkDispatcher<String>,
    val numSmallProducers: Int
) {

    private val throttle = Throttle<String>(20)
    private val bigProducerTracker = TaskRunTracker()
    private val smallProducerTracker = TaskRunTracker()

    fun run() {

        println("Running implementation: ${workDispatcher.javaClass.simpleName}")
        val producers = buildProducers()
        val startTime = System.currentTimeMillis()

        val producerThreads = producers.map{ producer -> Thread() { producer.run(workDispatcher) } }
        producerThreads.forEach { it.start() }
        producerThreads.forEach { it.join() }


        while (workDispatcher.hasWorkingTasks()) {
            Thread.sleep(10L)
        }
        val endTime = System.currentTimeMillis()
        val duration = (endTime - startTime)
        println("Run time: ${duration}ms")
        println("Peak thread count: ${workDispatcher.getPeakThreadCount()}")
        println("bigProducerTracker.start: ${bigProducerTracker.startTracker.getStats()}")
        println("bigProducerTracker.run: ${bigProducerTracker.runTracker.getStats()}")
        println("bigProducerTracker.total: ${bigProducerTracker.totalTracker.getStats()}")
        println("smallProducerTracker.start: ${smallProducerTracker.startTracker.getStats()}")
        println("smallProducerTracker.run: ${smallProducerTracker.runTracker.getStats()}")
        println("smallProducerTracker.total: ${smallProducerTracker.totalTracker.getStats()}")
    }

    private fun buildProducers(): List<Producer<String>> {
        val bigProducer = Producer(
            partitionKey = "BigProducer",
            numBatches = 10,
            tasksPerBatch = 1000,
            millisBetweenBatches = 1000L,
            taskDurationMillis = 1L,
            throttle = throttle,
            taskRunTracker = bigProducerTracker
        )

        val smallProducers = List(numSmallProducers) {
            Producer(
                partitionKey = "SmallProducer-$it",
                numBatches = 100,
                tasksPerBatch = 2,
                millisBetweenBatches = 50L,
                taskDurationMillis = 10L,
                throttle = throttle,
                taskRunTracker = smallProducerTracker
            )
        }

        return smallProducers + bigProducer
    }

    companion object {
        @JvmStatic
        fun main(args: Array<String>) {
            val numSmallProducers = 100
            val runBenchmark = RunBenchmark( SeparateThreadPoolsWorkDispatcher( 10), numSmallProducers)
            runBenchmark.run()
            val runBenchmark2 = RunBenchmark( ElasticThreadPoolWorkDispatcher(5, 500), numSmallProducers)
            runBenchmark2.run()

        }
    }
}

