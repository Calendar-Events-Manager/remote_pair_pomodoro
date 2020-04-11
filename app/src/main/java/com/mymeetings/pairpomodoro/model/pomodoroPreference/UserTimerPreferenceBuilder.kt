package com.mymeetings.pairpomodoro.model.pomodoroPreference

import android.content.Context
import android.content.SharedPreferences
import androidx.preference.PreferenceManager
import java.util.concurrent.TimeUnit

class UserTimerPreferenceBuilder {
    companion object {
        const val FOCUS_TIME_KEY = "focus_time"
        const val SHORT_BREAK_TIME = "short_break_time"
        const val LONG_BREAK_TIME = "long_break_time"
        const val SHORT_BREAK_COUNT = "short_break_count"

        fun build(context: Context): UserTimerPreference {
            val sharedPreferences: SharedPreferences =
                PreferenceManager.getDefaultSharedPreferences(
                    context
                )
            return UserTimerPreference(
                focusTime = TimeUnit.MINUTES.toMillis(
                    sharedPreferences.getInt(FOCUS_TIME_KEY, 25).toLong()
                ),
                shortBreakTime = TimeUnit.MINUTES.toMillis(
                    sharedPreferences.getInt(SHORT_BREAK_TIME, 5).toLong()
                ),
                longBreakTime = TimeUnit.MINUTES.toMillis(
                    sharedPreferences.getInt(LONG_BREAK_TIME, 15).toLong()
                ),
                shortBreakCount = sharedPreferences.getInt(SHORT_BREAK_COUNT, 3)
            )
        }

        fun build(timerPreference: TimerPreference) =
            UserTimerPreference(
                focusTime = timerPreference.getFocusTime(),
                shortBreakTime = timerPreference.getShortBreakTime(),
                longBreakTime = timerPreference.getLongBreakTime(),
                shortBreakCount = timerPreference.getShortBreakCount()
            )


        fun buildDefault(): UserTimerPreference {
            return UserTimerPreference(
                focusTime = TimeUnit.MINUTES.toMillis(25),
                shortBreakTime = TimeUnit.MINUTES.toMillis(5),
                longBreakTime = TimeUnit.MINUTES.toMillis(15),
                shortBreakCount = 3
            )
        }
    }
}