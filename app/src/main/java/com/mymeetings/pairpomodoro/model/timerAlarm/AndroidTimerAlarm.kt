package com.mymeetings.pairpomodoro.model.timerAlarm

import android.content.Context
import android.media.Ringtone
import com.mymeetings.pairpomodoro.view.SoundUtils

class AndroidTimerAlarm(private val context: Context) : TimerAlarm {


    override fun alarmForShortBreak() {
        SoundUtils.triggerAlarmSound(context)
    }

    override fun alarmForLongBreak() {
        SoundUtils.triggerAlarmSound(context)
    }

    override fun alarmForBreakOver() {
        SoundUtils.triggerAlarmSound(context)
    }

    override fun stopAlarm() {

    }
}