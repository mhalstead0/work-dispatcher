package com.matthalstead.workdispatcher

import com.matthalstead.workdispatcher.implementations.ElasticThreadPoolWorkDispatcher

class ElasticThreadPoolWorkDispatcherTest: WorkDispatcherTest() {
    override fun buildDispatcher(): WorkDispatcher<String> {
        return ElasticThreadPoolWorkDispatcher(threadsPerPartition = 5, commonPoolSize = 500)
    }

}