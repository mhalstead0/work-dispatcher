package com.matthalstead.workdispatcher

import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch

class SimpleCoroutineWorkDispatcher<K>: AbstractLocalWorkDispatcher<K>() {

    private val coroutineScope = GlobalScope

    override fun doDispatch(
        workingTask: WorkingTask<K>,
        onStarted: (WorkingTask<K>) -> Unit,
        onCompleted: (WorkingTask<K>) -> Unit
    ) {
        coroutineScope.launch {
            try {
                onStarted(workingTask)
                workingTask.task.run()
            } finally {
                onCompleted(workingTask)
            }
        }
    }

}