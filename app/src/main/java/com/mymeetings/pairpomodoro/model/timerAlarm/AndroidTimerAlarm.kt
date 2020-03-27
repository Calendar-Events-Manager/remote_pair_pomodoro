package com.mymeetings.pairpomodoro.model.timerAlarm

import android.content.Context
import android.media.Ringtone
import com.mymeetings.pairpomodoro.view.SoundUtils

class AndroidTimerAlarm(private val context: Context) : TimerAlarm {

    private var ringtone: Ringtone? = null

    override fun alarmForShortBreak() {
        ringtone = SoundUtils.triggerAlarmSound(context)
    }

    override fun alarmForLongBreak() {
        ringtone = SoundUtils.triggerAlarmSound(context)
    }

    override fun alarmForBreakOver() {
        ringtone = SoundUtils.triggerAlarmSound(context)
    }

    override fun stopAlarm() {
        ringtone?.let {
            SoundUtils.stopAlarm(it)
        }
        ringtone = null
    }
}