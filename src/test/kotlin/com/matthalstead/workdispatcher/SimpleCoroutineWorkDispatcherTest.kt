package com.matthalstead.workdispatcher

class SimpleCoroutineWorkDispatcherTest: WorkDispatcherTest() {
    override fun buildDispatcher(): WorkDispatcher<String> {
        return SimpleCoroutineWorkDispatcher()
    }

}