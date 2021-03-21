package com.matthalstead.workdispatcher
class SimpleThreadsWorkDispatcher<K>: AbstractLocalWorkDispatcher<K>() {

    private val maxThreadCount = MaxTracker(0)

    override fun doDispatch(
        workingTask: WorkingTask<K>,
        onStarted: (WorkingTask<K>) -> Unit,
        onCompleted: (WorkingTask<K>) -> Unit
    ) {
        val r = Runnable{
            maxThreadCount.increment()
            try {
                onStarted(workingTask)
                workingTask.task.run()
            } finally {
                maxThreadCount.decrement()
                onCompleted(workingTask)
            }
        }
        Thread(r).start()
    }

    override fun getPeakThreadCount() = maxThreadCount.getMax()


}