package com.mymeetings.pairpomodoro.utils

class ClockUtil {

    private var previousTime: Long = -1

    fun getDiff(): Long {
        val currentTime = System.currentTimeMillis()
        return if (previousTime == -1L) {
            previousTime = currentTime
            0L
        } else {
            val diff = currentTime - previousTime
            previousTime = currentTime
            diff
        }
    }
}