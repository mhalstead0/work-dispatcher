package com.matthalstead.workdispatcher.benchmarks

import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.Semaphore

class Throttle<K>(private val maxPerKey: Int) {
    private val map = ConcurrentHashMap<K, Semaphore>()

    fun take(key: K) {
        val semaphore = map.computeIfAbsent(key) { Semaphore(maxPerKey) }
        semaphore.acquire()
    }

    fun release(key: K) {
        val semaphore = map.computeIfAbsent(key) { Semaphore(maxPerKey) }
        semaphore.release()
    }

}