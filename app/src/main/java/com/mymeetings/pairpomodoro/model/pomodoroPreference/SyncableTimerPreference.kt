package com.mymeetings.pairpomodoro.model.pomodoroPreference

import com.google.firebase.database.PropertyName
import java.util.concurrent.TimeUnit

data class SyncableTimerPreference(
    @PropertyName("focusTime") private val focusTime: Long = TimeUnit.MINUTES.toMillis(25),
    @PropertyName("shortBreakTime") private val shortBreakTime: Long = TimeUnit.MINUTES.toMillis(3),
    @PropertyName("longBreakTime") private val longBreakTime: Long = TimeUnit.MINUTES.toMillis(10),
    @PropertyName("shortBreakCount") private val shortBreakCount: Int = 3
) : TimerPreference {

    override fun getFocusTime() = focusTime

    override fun getShortBreakTime() = shortBreakTime

    override fun getLongBreakTime() = longBreakTime

    override fun getShortBreakCount() = shortBreakCount
}


fun TimerPreference.toSyncableTimerPreference(): SyncableTimerPreference {
    return SyncableTimerPreference(
        focusTime = getFocusTime(),
        shortBreakTime = getShortBreakTime(),
        longBreakTime = getLongBreakTime(),
        shortBreakCount = getShortBreakCount()
    )
}