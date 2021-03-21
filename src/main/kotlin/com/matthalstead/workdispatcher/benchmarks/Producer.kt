package com.matthalstead.workdispatcher.benchmarks

import com.matthalstead.workdispatcher.Task
import com.matthalstead.workdispatcher.WorkDispatcher

class Producer<K>(
    val partitionKey: K,
    val numBatches: Int,
    val tasksPerBatch: Int,
    val millisBetweenBatches: Long,
    val taskDurationMillis: Long,
    val taskRunTracker: TaskRunTracker
) {
    fun run(workDispatcher: WorkDispatcher<K>) {
        repeat(numBatches) { batchIndex ->
            repeat(tasksPerBatch) { taskIndex ->
                workDispatcher.enqueue(
                    buildTask("$partitionKey/$batchIndex/$taskIndex")
                )
            }
        }
    }

    private fun buildTask(descriptor: String): Task<K> {
        val enqueueTime = System.currentTimeMillis()
        return object:Task<K>(partitionKey, descriptor) {
            override fun run() {
                val startTime = System.currentTimeMillis()
                taskRunTracker.startTracker.addPoint(startTime - enqueueTime)
                if (taskDurationMillis <= 0) {
                    Thread.yield()
                } else {
                    Thread.sleep(taskDurationMillis)
                }
                val endTime = System.currentTimeMillis()
                taskRunTracker.runTracker.addPoint(endTime - startTime)
                taskRunTracker.totalTracker.addPoint(endTime - enqueueTime)
            }

        }
    }
}