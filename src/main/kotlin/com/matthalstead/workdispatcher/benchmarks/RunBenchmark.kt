package com.matthalstead.workdispatcher.benchmarks

import com.matthalstead.workdispatcher.implementations.ElasticThreadPoolWorkDispatcher
import com.matthalstead.workdispatcher.implementations.SeparateCoroutinePoolsWorkDispatcher
import com.matthalstead.workdispatcher.implementations.SeparateThreadPoolsWorkDispatcher
import com.matthalstead.workdispatcher.WorkDispatcher

class RunBenchmark(
    private val workDispatcher: WorkDispatcher<String>,
    private val numLargeProducers: Int,
    private val numSmallProducers: Int,
    private val largeProducerBatchSize: Int,
    private val throttleSize: Int
) {

    private val throttle = Throttle<String>(throttleSize)
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
        val bigProducers = List(numLargeProducers) {
            Producer(
                partitionKey = "BigProducer-$it",
                numBatches = 10,
                tasksPerBatch = largeProducerBatchSize,
                millisBetweenBatches = 1000L,
                taskDurationMillis = 1L,
                throttle = throttle,
                taskRunTracker = bigProducerTracker
            )
        }

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

        return bigProducers + smallProducers
    }

    companion object {
        @JvmStatic
        fun main(args: Array<String>) {
            val dispatcherType = DispatcherType.valueOf(args[0].toUpperCase())
            val targetThreadCount = 1000

            val numLargeProducers = 5
            val numSmallProducers = 1000

            val largeProducerBatchSize = 1000

            val throttleSize = 50
            val workDispatcher = createDispatcher(
                dispatcherType = dispatcherType,
                targetThreadCount = targetThreadCount,
                partitionCount = numLargeProducers + numSmallProducers
            )
            val runBenchmark = RunBenchmark(
                workDispatcher = workDispatcher,
                numLargeProducers = numLargeProducers,
                numSmallProducers = numSmallProducers,
                throttleSize = throttleSize,
                largeProducerBatchSize = largeProducerBatchSize
            )
            runBenchmark.run()
        }

        private fun createDispatcher(
            dispatcherType: DispatcherType,
            targetThreadCount: Int,
            partitionCount: Int
        ): WorkDispatcher<String> {
            return when (dispatcherType) {
                DispatcherType.SEPARATE_THREAD_POOLS -> SeparateThreadPoolsWorkDispatcher(
                    poolSizePerPartitionKey = ceilDivide(targetThreadCount, partitionCount)
                )
                DispatcherType.SEPARATE_COROUTINE_POOLS -> SeparateCoroutinePoolsWorkDispatcher(
                    poolSizePerPartitionKey = ceilDivide(targetThreadCount, partitionCount)
                )
                DispatcherType.ELASTIC_THREAD_POOL -> {
                    val perPartition = 3
                    val remaining = targetThreadCount - (perPartition*partitionCount)
                    ElasticThreadPoolWorkDispatcher(perPartition, remaining)
                }
            }
        }

        private fun ceilDivide(numer: Int, denom: Int): Int {
            val div = numer / denom
            return if ((div * denom) < numer) (div+1) else div
        }
    }

    enum class DispatcherType {
        SEPARATE_THREAD_POOLS,
        SEPARATE_COROUTINE_POOLS,
        ELASTIC_THREAD_POOL
    }
}

