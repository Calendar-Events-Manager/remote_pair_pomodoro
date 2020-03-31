package com.mymeetings.pairpomodoro.model.pomodoroAlarm

import android.content.Context
import android.content.res.AssetFileDescriptor
import android.media.MediaPlayer

class AndroidTimerAlarm(private val context: Context) : TimerAlarm {

    private val audioFileName = "alarm_ring_short.ogg"

    override fun alarm(timerAlarmType: TimerAlarmType) {
        val afd: AssetFileDescriptor = context.assets.openFd(audioFileName)
        val player = MediaPlayer()
        player.setDataSource(afd.fileDescriptor, afd.startOffset, afd.length)
        player.prepare()
        player.start()
    }
}