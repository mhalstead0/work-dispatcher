package com.matthalstead.workdispatcher

import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch

class SimpleCoroutineWorkDispatcher<K>: AbstractLocalWorkDispatcher<K>() {

    private val coroutineScope = GlobalScope

    private val maxThreadCount = MaxTracker(0)

    override fun doDispatch(
        workingTask: WorkingTask<K>,
        onStarted: (WorkingTask<K>) -> Unit,
        onCompleted: (WorkingTask<K>) -> Unit
    ) {
        coroutineScope.launch {
            maxThreadCount.increment()
            try {
                onStarted(workingTask)
                workingTask.task.run()
            } finally {
                maxThreadCount.decrement()
                onCompleted(workingTask)
            }
        }
    }


    override fun getPeakThreadCount() = maxThreadCount.getMax()

}