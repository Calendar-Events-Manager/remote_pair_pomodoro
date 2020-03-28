package com.mymeetings.pairpomodoro.model.timerAlarm

import android.content.Context
import com.mymeetings.pairpomodoro.view.SoundUtils

class AndroidTimerAlarm(private val context: Context) : TimerAlarm {

    override fun alarm(timerAlarmType: TimerAlarmType) {
        SoundUtils.triggerAlarmSound(context)
    }
}