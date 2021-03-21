package com.matthalstead.workdispatcher

import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors
import java.util.concurrent.ThreadFactory
import java.util.concurrent.atomic.AtomicLong

class SeparateThreadPoolsWorkDispatcher<K>(val poolSizePerPartitionKey: Int): AbstractLocalWorkDispatcher<K>() {

    private val executorServiceMap = ConcurrentHashMap<K, ExecutorService>()
    private val maxThreadCounter = MaxTracker(0)

    override fun shutdown() {
        executorServiceMap.values.forEach{ it.shutdown() }
        executorServiceMap.clear()
    }

    override fun doDispatch(
        workingTask: WorkingTask<K>,
        onStarted: (WorkingTask<K>) -> Unit,
        onCompleted: (WorkingTask<K>) -> Unit
    ) {
        val r = Runnable{
            try {
                onStarted(workingTask)
                workingTask.task.run()
            } finally {
                onCompleted(workingTask)
            }
        }

        val executorService = executorServiceMap.computeIfAbsent(workingTask.task.partitionKey) { buildExecutor(it) }
        executorService.submit(r)
    }

    private fun buildExecutor(partitionKey: K): ExecutorService {
        val threadIndexGenerator = AtomicLong(0L)
        val threadFactory = ThreadFactory { r ->
            val t = Thread(r, "com.matthalstead.workdispatcher.ThreadPoolWorkDispatcher-$partitionKey-${threadIndexGenerator.incrementAndGet()}")
            t.isDaemon = true
            t
        }
        maxThreadCounter.add(poolSizePerPartitionKey)
        return Executors.newFixedThreadPool(poolSizePerPartitionKey, threadFactory)
    }

    override fun getPeakThreadCount() = maxThreadCounter.getMax()

}