package com.mymeetings.pairpomodoro.model.timerPreference

import java.util.concurrent.TimeUnit

data class DefaultTimerPreference(
    private val focusTime: Long = TimeUnit.MINUTES.toMillis(25),
    private val shortBreakTime: Long = TimeUnit.MINUTES.toMillis(3),
    private val longBreakTime: Long = TimeUnit.MINUTES.toMillis(10),
    private val shortBreakCount: Int = 3
) : TimerPreference {

    constructor() : this(
        focusTime = TimeUnit.MINUTES.toMillis(25),
        shortBreakTime = TimeUnit.MINUTES.toMillis(3),
        longBreakTime = TimeUnit.MINUTES.toMillis(10),
        shortBreakCount = 3
    )

    override fun getFocusTime() = focusTime

    override fun getShortBreakTime() = shortBreakTime

    override fun getLongBreakTime() = longBreakTime

    override fun getShortBreakCount() = shortBreakCount
}