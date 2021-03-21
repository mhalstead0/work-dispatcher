package com.matthalstead.workdispatcher
class SimpleThreadsWorkDispatcher<K>: AbstractLocalWorkDispatcher<K>() {

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
        Thread(r).start()
    }

}