package com.matthalstead.workdispatcher

interface WorkDispatcher<K> {

    fun enqueue(task: Task<K>)

    fun getTaskReport(): List<TaskReportRecord<K>>

    fun start() {}
    fun shutdown() {}
}

enum class TaskStatus { ENQUEUED, RUNNING }

data class TaskReportRecord<K>(val partitionKey: K, val descriptor: Any, val taskStatus: TaskStatus)

abstract class Task<K>(val partitionKey: K, val descriptor: Any): Runnable {

}