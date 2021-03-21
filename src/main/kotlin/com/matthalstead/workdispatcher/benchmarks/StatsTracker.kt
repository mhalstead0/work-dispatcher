package com.matthalstead.workdispatcher.benchmarks

import java.util.concurrent.ConcurrentSkipListMap
import java.util.concurrent.atomic.AtomicInteger

class TaskRunTracker {
    val startTracker = StatsTracker()
    val runTracker = StatsTracker()
    val totalTracker = StatsTracker()
}

class StatsTracker {

    private val valueMap = ConcurrentSkipListMap<Long, AtomicInteger>()

    fun addPoint(x: Long) {
        valueMap.computeIfAbsent(x) { AtomicInteger(0) }.incrementAndGet()
    }

    fun getStats(): Stats {
        val count = valueMap.values.sumBy { it.get() }
        return if (count == 0) {
            Stats(
                count = 0,
                min = null,
                max = null,
                average = null,
                median = null
            )
        } else {
            val min = valueMap.firstKey()
            val max = valueMap.lastKey()

            var median: Long? = null

            val medianIndex = count / 2
            var counter = 0
            var sum = 0L
            valueMap.forEach { (x, num) ->
                val n = num.get()

                if ((medianIndex >= counter) && (medianIndex < (counter + n))) {
                    median = x
                }

                counter += n
                sum += (x * n.toLong())
            }
            Stats(
                count = counter,
                min = min,
                max = max,
                average = sum.toDouble() / counter.toDouble(),
                median = median
            )
        }
    }

}

data class Stats(
    val count: Int,
    val min: Long?,
    val max: Long?,
    val average: Double?,
    val median: Long?
)