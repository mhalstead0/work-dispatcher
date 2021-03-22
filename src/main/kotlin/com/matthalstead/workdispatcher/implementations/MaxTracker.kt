package com.matthalstead.workdispatcher.implementations

import java.util.concurrent.atomic.AtomicInteger

class MaxTracker(initialValue: Int) {

    private val max = AtomicInteger(initialValue)
    private val currentValue = AtomicInteger(initialValue)

    fun increment() = add(1)

    fun add(count: Int) {
        val newValue = currentValue.addAndGet(count)
        while (true) {
            val oldMax = max.get()
            if (newValue <= oldMax) {
                return
            } else {
                val casSuccess = max.compareAndSet(oldMax, newValue)
                if (casSuccess) {
                    return
                }
            }
        }
    }

    fun decrement() {
        currentValue.decrementAndGet()
    }

    fun subtract(count: Int) = add(-count)

    fun getMax() = max.get()
}