package com.mymeetings.pairpomodoro.model.pomodoroPreference

import android.content.Context
import android.content.SharedPreferences
import androidx.preference.PreferenceManager
import java.util.concurrent.TimeUnit

data class UserTimerPreference(val context: Context) : TimerPreference {

    private val sharedPreferences: SharedPreferences =
        PreferenceManager.getDefaultSharedPreferences(context)

    override fun getFocusTime() =
        TimeUnit.MINUTES.toMillis(sharedPreferences.getInt("focus_time", 25).toLong())

    override fun getShortBreakTime() =
        TimeUnit.MINUTES.toMillis(sharedPreferences.getInt("short_break_time", 5).toLong())

    override fun getLongBreakTime() =
        TimeUnit.MINUTES.toMillis(sharedPreferences.getInt("long_break_time", 15).toLong())

    override fun getShortBreakCount() = sharedPreferences.getInt("short_break_count", 3)
}