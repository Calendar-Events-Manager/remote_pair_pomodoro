package com.mymeetings.pairpomodoro.model.pomodoroPreference

interface TimerPreference {

    fun getFocusTime() : Long

    fun getShortBreakTime() : Long

    fun getLongBreakTime() : Long

    fun getShortBreakCount() : Int
}