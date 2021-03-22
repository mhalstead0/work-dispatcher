package com.matthalstead.workdispatcher

import com.matthalstead.workdispatcher.implementations.SimpleCoroutineWorkDispatcher

class SimpleCoroutineWorkDispatcherTest: WorkDispatcherTest() {
    override fun buildDispatcher(): WorkDispatcher<String> {
        return SimpleCoroutineWorkDispatcher()
    }

}