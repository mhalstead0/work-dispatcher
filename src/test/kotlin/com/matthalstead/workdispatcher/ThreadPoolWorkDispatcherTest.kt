package com.matthalstead.workdispatcher

class ThreadPoolWorkDispatcherTest: WorkDispatcherTest() {
    override fun buildDispatcher(): WorkDispatcher<String> {
        return ThreadPoolWorkDispatcher(poolSize = 100)
    }

}