package com.matthalstead.workdispatcher.implementations

import java.util.concurrent.TimeUnit
import java.util.concurrent.atomic.AtomicBoolean
import java.util.concurrent.locks.Lock
import java.util.concurrent.locks.ReentrantLock
import kotlin.concurrent.withLock

class ElasticThreadPoolWorkDispatcher<K>(val threadsPerPartition: Int, val commonPoolSize: Int): AbstractLocalWorkDispatcher<K>() {

    private var commonExecutor = CommonExecutor(commonPoolSize) { takeTaskFromAnyPartition() }

    private val taskQueueMap = mutableMapOf<K, MutableList<TaskQueueEntry<K>>>()
    private val taskQueueLock: Lock = ReentrantLock()

    private val partitionExecutorMap = mutableMapOf<K, PartitionExecutor<K>>()
    private val partitionExecutorMapLock: Lock = ReentrantLock()

    private val threadCountTracker = MaxTracker(commonPoolSize)

    override fun start() {
        commonExecutor.start()
    }

    override fun shutdown() {
        commonExecutor.start()
        partitionExecutorMap.values.forEach { it.stop() }
    }

    private fun takeTask(partitionKey: K): TaskQueueEntry<K>? =
        taskQueueLock.withLock {
            val queue = taskQueueMap[partitionKey]
            return if (queue.isNullOrEmpty()) {
                null
            } else {
                queue.removeAt(0)
            }
        }

    private fun takeTaskFromAnyPartition(): TaskQueueEntry<K>? =
        taskQueueLock.withLock {
            val queue = taskQueueMap.values.firstOrNull{ it.isNotEmpty() }
            return queue?.removeAt(0)
        }

    override fun doDispatch(
            workingTask: WorkingTask<K>,
            onStarted: (WorkingTask<K>) -> Unit,
            onCompleted: (WorkingTask<K>) -> Unit
    ) {
        val partitionKey = workingTask.task.partitionKey
        taskQueueLock.withLock {
            val taskQueue = taskQueueMap.computeIfAbsent(partitionKey) { mutableListOf() }
            taskQueue.add(TaskQueueEntry(workingTask, onStarted, onCompleted))
        }

        val queueFunction = this::takeTask
        val partitionExecutor = partitionExecutorMapLock.withLock {
            partitionExecutorMap.computeIfAbsent(partitionKey) {
                val pe = PartitionExecutor(it, threadsPerPartition, queueFunction)
                pe.start()
                threadCountTracker.add(threadsPerPartition)
                pe
            }
        }

        partitionExecutor.signalAllThreads()
        commonExecutor.signalAllThreads()

    }

    override fun getPeakThreadCount() = threadCountTracker.getMax()


    private class TaskQueueEntry<K>(
            val workingTask: WorkingTask<K>,
            private val onStarted: (WorkingTask<K>) -> Unit,
            private val onCompleted: (WorkingTask<K>) -> Unit
    ) {
        fun started() = onStarted(workingTask)
        fun completed() = onCompleted(workingTask)
    }

    private abstract class TaskExecutor<K>(
        val poolSize: Int
    ) {

        private val stopped = AtomicBoolean(false)
        private val lock: Lock = ReentrantLock()
        private val condition = lock.newCondition()

        fun start() {
            repeat(poolSize) {
                val t = Thread( { runLoop() }, getThreadName(it))
                t.isDaemon = true
                t.start()
            }
        }

        fun signalAllThreads() {
            lock.withLock {
                condition.signal()
            }
        }

        abstract fun getThreadName(index: Int): String
        abstract fun takeTask(): TaskQueueEntry<K>?

        fun stop() {
            stopped.set(true)
            lock.withLock {
                condition.signalAll()
            }
        }

        private fun runLoop() {
            while (!stopped.get()) {
                val taskQueueEntry = takeTask()

                if (taskQueueEntry != null) {
                    taskQueueEntry.started()
                    try {
                        taskQueueEntry.workingTask.task.run()
                    } catch (t: Throwable) {
                        println("Caught an exception running a task: $t")
                        t.printStackTrace()
                    } finally {
                        taskQueueEntry.completed()
                    }
                }

                val shouldSleep = (taskQueueEntry == null)
                if (shouldSleep) {
                    lock.withLock {
                        if (!stopped.get()) {
                            condition.await(30L, TimeUnit.SECONDS)
                        }
                    }
                }
            }
        }

    }

    private class CommonExecutor<K>(
        poolSize: Int,
        val queue: () -> TaskQueueEntry<K>?
    ): TaskExecutor<K>(poolSize) {

        override fun getThreadName(index: Int) = "CommonExecutor-$index"

        override fun takeTask() = queue()
    }

    private class PartitionExecutor<K>(
        val partitionKey: K,
        poolSize: Int,
        val queue: (K) -> TaskQueueEntry<K>?
    ): TaskExecutor<K>(poolSize) {

        override fun getThreadName(index: Int) = "PartitionExecutor-$partitionKey-$index"

        override fun takeTask() = queue(partitionKey)
    }

}