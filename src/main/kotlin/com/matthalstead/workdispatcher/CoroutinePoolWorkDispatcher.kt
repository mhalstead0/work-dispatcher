package com.matthalstead.workdispatcher

import kotlinx.coroutines.*
import java.util.concurrent.Executors

class CoroutinePoolWorkDispatcher<K>(val poolSize: Int): AbstractLocalWorkDispatcher<K>() {

    private var coroutineDispatcher: ExecutorCoroutineDispatcher? = null
    private var coroutineScope: CoroutineScope? = null


    override fun start() {
        coroutineDispatcher = Executors.newFixedThreadPool(poolSize).asCoroutineDispatcher()
        coroutineScope = CoroutineScope(coroutineDispatcher!!)
    }

    override fun shutdown() {
        coroutineScope = null
        coroutineDispatcher!!.close()
        coroutineDispatcher = null
    }

    override fun getPeakThreadCount() = poolSize

    override fun doDispatch(
        workingTask: WorkingTask<K>,
        onStarted: (WorkingTask<K>) -> Unit,
        onCompleted: (WorkingTask<K>) -> Unit
    ) {
        coroutineScope!!.launch {
            try {
                onStarted(workingTask)
                workingTask.task.run()
            } finally {
                onCompleted(workingTask)
            }
        }
    }

}