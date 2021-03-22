package com.matthalstead.workdispatcher

import com.matthalstead.workdispatcher.implementations.SeparateCoroutinePoolsWorkDispatcher

class SeparateCoroutinePoolsWorkDispatcherTest: WorkDispatcherTest() {
    override fun buildDispatcher(): WorkDispatcher<String> {
        return SeparateCoroutinePoolsWorkDispatcher(poolSizePerPartitionKey = 50)
    }

}