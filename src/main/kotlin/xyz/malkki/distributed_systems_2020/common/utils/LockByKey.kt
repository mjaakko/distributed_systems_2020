package xyz.malkki.distributed_systems_2020.common.utils

import java.util.concurrent.locks.ReentrantLock
import kotlin.concurrent.withLock

/**
 * Helper for running code synchronously
 */
class LockByKey<T> {
    private val lockedKeys = mutableSetOf<T>()
    private val reentrantLock = ReentrantLock()
    private val condition = reentrantLock.newCondition()

    private fun lock(key: T) {
        reentrantLock.withLock {
            while(!lockedKeys.add(key)) {
                condition.await()
            }
        }
    }

    private fun unlock(key: T) {
        reentrantLock.withLock {
            lockedKeys.remove(key)
            condition.signalAll()
        }
    }

    fun <R> run(key: T, block: () -> R): R {
        try {
            lock(key)
            return block()
        } finally {
            unlock(key)
        }
    }
}