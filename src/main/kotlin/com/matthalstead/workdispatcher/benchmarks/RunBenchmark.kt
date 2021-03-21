package com.matthalstead.workdispatcher.benchmarks

import com.matthalstead.workdispatcher.ElasticThreadPoolWorkDispatcher
import com.matthalstead.workdispatcher.SeparateThreadPoolsWorkDispatcher
import com.matthalstead.workdispatcher.Task
import com.matthalstead.workdispatcher.WorkDispatcher

class RunBenchmark(
    val workDispatcher: WorkDispatcher<String>
) {

    val bigProducerTracker = TaskRunTracker()
    val smallProducerTracker = TaskRunTracker()

    fun run() {
        val producers = buildProducers()
        val startTime = System.currentTimeMillis()
        producers.forEach { it.run(workDispatcher) }

        while (workDispatcher.hasWorkingTasks()) {
            Thread.sleep(10L)
        }
        val endTime = System.currentTimeMillis()
        println("Implementation: ${workDispatcher.javaClass.simpleName}")
        println("Run time: ${endTime - startTime}ms")
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
            millisBetweenBatches = 5000L,
            taskDurationMillis = 1L,
            taskRunTracker = bigProducerTracker
        )

        val smallProducers = List(100) {
            Producer(
                partitionKey = "SmallProducer-$it",
                numBatches = 100,
                tasksPerBatch = 2,
                millisBetweenBatches = 50L,
                taskDurationMillis = 10L,
                taskRunTracker = smallProducerTracker
            )
        }

        return smallProducers + bigProducer
    }

    companion object {
        @JvmStatic
        fun main(args: Array<String>) {
            val runBenchmark = RunBenchmark( SeparateThreadPoolsWorkDispatcher( 50))
            runBenchmark.run()
            val runBenchmark2 = RunBenchmark( ElasticThreadPoolWorkDispatcher(5, 500))
            runBenchmark2.run()
        }
    }
}

