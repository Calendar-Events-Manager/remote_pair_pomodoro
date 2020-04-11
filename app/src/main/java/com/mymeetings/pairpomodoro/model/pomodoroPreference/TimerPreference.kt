package com.mymeetings.pairpomodoro.model.pomodoroPreference

import android.os.Parcelable

interface TimerPreference: Parcelable {

    fun getFocusTime() : Long

    fun getShortBreakTime() : Long

    fun getLongBreakTime() : Long

    fun getShortBreakCount() : Int
}