package com.matthalstead.workdispatcher

class CoroutinePoolWorkDispatcherTest: WorkDispatcherTest() {
    override fun buildDispatcher(): WorkDispatcher<String> {
        return CoroutinePoolWorkDispatcher(poolSize = 100)
    }

}