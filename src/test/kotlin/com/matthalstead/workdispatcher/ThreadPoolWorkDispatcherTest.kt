package com.matthalstead.workdispatcher

import com.matthalstead.workdispatcher.implementations.ThreadPoolWorkDispatcher

class ThreadPoolWorkDispatcherTest: WorkDispatcherTest() {
    override fun buildDispatcher(): WorkDispatcher<String> {
        return ThreadPoolWorkDispatcher(poolSize = 100)
    }

}