package com.matthalstead.workdispatcher

class SeparateThreadPoolsWorkDispatcherTest: WorkDispatcherTest() {
    override fun buildDispatcher(): WorkDispatcher<String> {
        return SeparateThreadPoolsWorkDispatcher(poolSizePerPartitionKey = 50)
    }

}