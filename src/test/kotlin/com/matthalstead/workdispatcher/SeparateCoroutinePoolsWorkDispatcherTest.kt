package com.matthalstead.workdispatcher

class SeparateCoroutinePoolsWorkDispatcherTest: WorkDispatcherTest() {
    override fun buildDispatcher(): WorkDispatcher<String> {
        return SeparateCoroutinePoolsWorkDispatcher(poolSizePerPartitionKey = 50)
    }

}