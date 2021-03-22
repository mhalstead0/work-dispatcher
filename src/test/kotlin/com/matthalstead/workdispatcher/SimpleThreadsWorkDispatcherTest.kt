package com.matthalstead.workdispatcher

import com.matthalstead.workdispatcher.implementations.SimpleThreadsWorkDispatcher

class SimpleThreadsWorkDispatcherTest: WorkDispatcherTest() {
    override fun buildDispatcher(): WorkDispatcher<String> {
        return SimpleThreadsWorkDispatcher()
    }

}