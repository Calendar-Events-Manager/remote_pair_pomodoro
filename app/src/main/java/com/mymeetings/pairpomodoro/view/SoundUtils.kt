package com.mymeetings.pairpomodoro.view

import android.content.Context
import android.media.Ringtone
import android.media.RingtoneManager
import android.net.Uri


object SoundUtils {

    private var ringTone : Ringtone? = null

    fun triggerAlarmSound(context: Context)  {
        var alarmUri: Uri? = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM)
        if (alarmUri == null) {
            alarmUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_RINGTONE)
        }
        val ringtone = RingtoneManager.getRingtone(context, alarmUri)
        ringtone.play()

        this.ringTone = ringtone
    }

    fun stopAlarm() {
        this.ringTone?.stop()
    }
}