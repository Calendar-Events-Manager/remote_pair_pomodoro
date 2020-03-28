package com.mymeetings.pairpomodoro.view

import android.content.Context
import android.os.PowerManager

object WakeLockUtil {

    private const val WAKE_LOCK_TAG = "pomodoro:cpuwake"

    fun makeCPUPowerInWake(context: Context, timeToKeepLock: Long): PowerManager.WakeLock {
        return (context.getSystemService(Context.POWER_SERVICE) as PowerManager).run {
            newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, WAKE_LOCK_TAG).apply {
                acquire(timeToKeepLock)
            }
        }
    }
}