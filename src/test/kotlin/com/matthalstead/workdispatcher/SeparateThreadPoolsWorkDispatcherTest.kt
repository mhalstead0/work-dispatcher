package com.matthalstead.workdispatcher

import com.matthalstead.workdispatcher.implementations.SeparateThreadPoolsWorkDispatcher

class SeparateThreadPoolsWorkDispatcherTest: WorkDispatcherTest() {
    override fun buildDispatcher(): WorkDispatcher<String> {
        return SeparateThreadPoolsWorkDispatcher(poolSizePerPartitionKey = 50)
    }

}