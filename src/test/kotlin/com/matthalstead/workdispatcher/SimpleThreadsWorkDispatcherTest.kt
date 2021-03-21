package com.matthalstead.workdispatcher

class SimpleThreadsWorkDispatcherTest: WorkDispatcherTest() {
    override fun buildDispatcher(): WorkDispatcher<String> {
        return SimpleThreadsWorkDispatcher()
    }

}