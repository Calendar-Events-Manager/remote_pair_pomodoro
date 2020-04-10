package com.mymeetings.pairpomodoro.model.pomodoroPreference

import kotlinx.android.parcel.Parcelize
import java.util.concurrent.TimeUnit

@Parcelize
data class UserTimerPreference(
    private val focusTime: Long = TimeUnit.MINUTES.toMillis(25),
    private val shortBreakTime: Long = TimeUnit.MINUTES.toMillis(3),
    private val longBreakTime: Long = TimeUnit.MINUTES.toMillis(10),
    private val shortBreakCount: Int = 3
) : TimerPreference {

    override fun getFocusTime() = focusTime

    override fun getShortBreakTime() = shortBreakTime

    override fun getLongBreakTime() = longBreakTime

    override fun getShortBreakCount() = shortBreakCount
}

