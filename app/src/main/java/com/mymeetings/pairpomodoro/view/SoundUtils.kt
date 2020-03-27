package com.mymeetings.pairpomodoro.view

import android.content.Context
import android.media.Ringtone
import android.media.RingtoneManager
import android.net.Uri


object SoundUtils {

    fun triggerAlarmSound(context: Context): Ringtone {
        var alarmUri: Uri? = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM)
        if (alarmUri == null) {
            alarmUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_RINGTONE)
        }
        val ringtone = RingtoneManager.getRingtone(context, alarmUri)
        ringtone.play()

        return ringtone
    }

    fun stopAlarm(ringtone: Ringtone) {
        ringtone.stop()
    }
}