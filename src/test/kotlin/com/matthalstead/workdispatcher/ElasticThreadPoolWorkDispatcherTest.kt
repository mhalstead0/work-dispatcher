package com.matthalstead.workdispatcher

class ElasticThreadPoolWorkDispatcherTest: WorkDispatcherTest() {
    override fun buildDispatcher(): WorkDispatcher<String> {
        return ElasticThreadPoolWorkDispatcher(threadsPerPartition = 5, commonPoolSize = 500)
    }

}