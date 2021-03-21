package com.matthalstead.workdispatcher

import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors
import java.util.concurrent.ThreadFactory
import java.util.concurrent.atomic.AtomicLong

class ThreadPoolWorkDispatcher<K>(val poolSize: Int): AbstractLocalWorkDispatcher<K>() {

    private var executorService: ExecutorService? = null

    override fun start() {
        val threadIndexGenerator = AtomicLong(0L)
        val threadFactory = ThreadFactory { r ->
            val t = Thread(r, "com.matthalstead.workdispatcher.ThreadPoolWorkDispatcher-${threadIndexGenerator.incrementAndGet()}")
            t.isDaemon = true
            t
        }
        executorService = Executors.newFixedThreadPool(poolSize, threadFactory)
    }

    override fun getPeakThreadCount() = poolSize

    override fun shutdown() {
        executorService!!.shutdown()
        executorService = null
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
        executorService!!.submit(r)
    }

}