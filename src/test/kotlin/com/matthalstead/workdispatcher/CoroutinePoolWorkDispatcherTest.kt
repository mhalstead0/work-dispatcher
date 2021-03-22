package com.matthalstead.workdispatcher

import com.matthalstead.workdispatcher.implementations.CoroutinePoolWorkDispatcher

class CoroutinePoolWorkDispatcherTest: WorkDispatcherTest() {
    override fun buildDispatcher(): WorkDispatcher<String> {
        return CoroutinePoolWorkDispatcher(poolSize = 100)
    }

}