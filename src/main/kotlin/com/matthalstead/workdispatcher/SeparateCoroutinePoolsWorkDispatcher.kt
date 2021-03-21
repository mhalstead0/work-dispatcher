package com.matthalstead.workdispatcher

import kotlinx.coroutines.*
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.Executors

class SeparateCoroutinePoolsWorkDispatcher<K>(val poolSizePerPartitionKey: Int): AbstractLocalWorkDispatcher<K>() {

    private val dispatcherMap = ConcurrentHashMap<K, CoroutineScopeAndDispatcher>()

    override fun shutdown() {
        dispatcherMap.values.forEach{ it.coroutineDispatcher.close() }
        dispatcherMap.clear()
    }

    override fun doDispatch(
        workingTask: WorkingTask<K>,
        onStarted: (WorkingTask<K>) -> Unit,
        onCompleted: (WorkingTask<K>) -> Unit
    ) {
        val scopeAndDispatcher = dispatcherMap.computeIfAbsent(workingTask.task.partitionKey) { buildDispatcher() }
        scopeAndDispatcher.coroutineScope.launch {
            try {
                onStarted(workingTask)
                workingTask.task.run()
            } finally {
                onCompleted(workingTask)
            }
        }
    }

    private fun buildDispatcher(): CoroutineScopeAndDispatcher {
        val coroutineDispatcher = Executors.newFixedThreadPool(poolSizePerPartitionKey).asCoroutineDispatcher()
        val coroutineScope = CoroutineScope(coroutineDispatcher)
        return CoroutineScopeAndDispatcher(coroutineScope, coroutineDispatcher)
    }

    private data class CoroutineScopeAndDispatcher(
        val coroutineScope: CoroutineScope,
        val coroutineDispatcher: ExecutorCoroutineDispatcher
    )

}