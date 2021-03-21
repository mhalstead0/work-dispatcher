package com.matthalstead.workdispatcher

import java.util.concurrent.atomic.AtomicLong
import java.util.concurrent.locks.ReadWriteLock
import java.util.concurrent.locks.ReentrantReadWriteLock
import kotlin.concurrent.withLock

abstract class AbstractLocalWorkDispatcher<K>: WorkDispatcher<K> {

    private val idGenerator = AtomicLong(0)
    private val taskMap = mutableMapOf<Long, WorkingTask<K>>()
    private val mapRWL: ReadWriteLock = ReentrantReadWriteLock()
    private val mapReadLock = mapRWL.readLock()
    private val mapWriteLock = mapRWL.writeLock()

    override fun enqueue(task: Task<K>) {
        val id = idGenerator.incrementAndGet()
        val workingTask = WorkingTask(id, task)
        mapWriteLock.withLock {
            taskMap[id] = workingTask
        }

        doDispatch(
            workingTask,
            { it.started = true },
            { mapWriteLock.withLock { taskMap.remove(it.id) } }
        )
    }

    override fun getTaskReport(): List<TaskReportRecord<K>> =
        mapReadLock.withLock {
            taskMap.values.map { it.toTaskReportRecord() }
        }

    override fun hasWorkingTasks(): Boolean =
        mapReadLock.withLock {
            taskMap.isNotEmpty()
        }

    protected abstract fun doDispatch(
        workingTask: WorkingTask<K>,
        onStarted: (WorkingTask<K>) -> Unit,
        onCompleted: (WorkingTask<K>) -> Unit
    )



    protected class WorkingTask<K>(
        val id: Long,
        val task: Task<K>,
        var started: Boolean = false
    ) {
        fun toTaskReportRecord() = TaskReportRecord<K>(
            partitionKey = task.partitionKey,
            descriptor = task.descriptor,
            taskStatus = if (started) TaskStatus.RUNNING else TaskStatus.ENQUEUED
        )

    }
}