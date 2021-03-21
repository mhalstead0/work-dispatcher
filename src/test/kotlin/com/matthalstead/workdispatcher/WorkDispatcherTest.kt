package com.matthalstead.workdispatcher

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInfo
import java.util.concurrent.atomic.AtomicBoolean
import java.util.concurrent.atomic.AtomicInteger

abstract class WorkDispatcherTest {

    abstract fun buildDispatcher(): WorkDispatcher<String>

    private fun runTest(f: (WorkDispatcher<String>) -> Unit) {
        val dispatcher = buildDispatcher()
        dispatcher.start()
        try {
            f(dispatcher)
        } finally {
            dispatcher.shutdown()
        }
    }

    private fun simpleTest(testInfo: TestInfo, taskCount: Int, partitionKeyCount: Int) {
        runTest { dispatcher ->
            val startTime = System.currentTimeMillis()
            val tasks = buildTasks(taskCount, partitionKeyCount)
            tasks.forEach { dispatcher.enqueue(it) }

            while (dispatcher.getTaskReport().isNotEmpty()) {
                Thread.sleep(10L)
            }
            val endTime  = System.currentTimeMillis()

            tasks.forEach {
                assertThat(it.startCount.get()).isEqualTo(1)
                assertThat(it.done).isTrue
            }
            log(testInfo, "Time to process $taskCount tasks was ${endTime-startTime}ms")
        }
    }

    @Test
    fun `simple test`(testInfo: TestInfo) {
        simpleTest(testInfo, 100, 10)
    }

    @Test
    fun `ten thousand tasks`(testInfo: TestInfo) {
        simpleTest(testInfo, 10000, 10)
    }

    @Test
    fun `hundred thousand tasks`(testInfo: TestInfo) {
        simpleTest(testInfo, 100000, 10)
    }

    @Test
    fun `small producer wait time against large producer`(testInfo: TestInfo) {
        runTest { dispatcher ->
            val largeProducerTasks = buildTasks(taskCount = 1000, partitionKeyCount = 1, sleepTimeMillis = 10L)
            val smallProducerTask = TestTask("SmallProducer", "Small Producer com.matthalstead.workdispatcher.Task", 1L)

            largeProducerTasks.forEach { dispatcher.enqueue(it) }
            val smallProducerStartTime = System.currentTimeMillis()
            dispatcher.enqueue(smallProducerTask)

            while (dispatcher.getTaskReport().isNotEmpty()) {
                Thread.sleep(10L)
            }

            assertThat(smallProducerTask.done).isTrue

            val smallProducerWaitTime = smallProducerTask.startTime!! - smallProducerStartTime
            log(testInfo, "Small producer delayed by ${smallProducerWaitTime}ms")

        }
    }

    @Test
    fun `one large producer and many small producers`(testInfo: TestInfo) {
        runTest { dispatcher ->
            val startTime = System.currentTimeMillis()
            val bigProducerPartitionKey = "BigProducer"
            repeat(100) { loopCount ->
                repeat(100) { taskCount ->
                    dispatcher.enqueue(TestTask(bigProducerPartitionKey, "task-$loopCount-$taskCount", 10L))
                }
                repeat(100) { producerIndex ->
                    dispatcher.enqueue(TestTask("producer-$producerIndex", "task-$loopCount-$producerIndex", 10L))
                }
            }
            val beforeLastTaskTime = System.currentTimeMillis()
            val lastTask = TestTask("LastKey", "LastTask", 1L)
            dispatcher.enqueue(lastTask)

            while (dispatcher.getTaskReport().isNotEmpty()) {
                Thread.sleep(10L)
            }

            val endTime = System.currentTimeMillis()
            log(testInfo, "Total time for large-producer with many small: ${endTime - startTime}ms")
            log(testInfo, "Last task delayed by ${lastTask.startTime!! - beforeLastTaskTime}ms")

        }
    }

    private fun log(testInfo: TestInfo, str: String) {
        println("${getTestName(testInfo)}: $str")
    }

    private fun getTestName(testInfo: TestInfo): String = testInfo.testClass.orElse(WorkDispatcherTest::class.java).name



    private fun buildTasks(taskCount: Int, partitionKeyCount: Int, sleepTimeMillis: Long = 1L) =
        List(taskCount) { TestTask("Key${it % partitionKeyCount}", "com.matthalstead.workdispatcher.Task$taskCount", sleepTimeMillis) }



    private class TestTask(
        partitionKey: String,
        descriptor: String,
        private val sleepTimeMillis: Long
    ): Task<String>(partitionKey, descriptor) {

        val startCount = AtomicInteger(0)
        var startTime: Long? = null
        val done = AtomicBoolean(false)

        override fun run() {
            check(startCount.incrementAndGet() == 1) { "com.matthalstead.workdispatcher.Task was started twice" }
            startTime = System.currentTimeMillis()
            if (sleepTimeMillis <= 0L) {
                Thread.yield()
            } else {
                Thread.sleep(sleepTimeMillis)
            }
            done.set(true)
        }

    }

}