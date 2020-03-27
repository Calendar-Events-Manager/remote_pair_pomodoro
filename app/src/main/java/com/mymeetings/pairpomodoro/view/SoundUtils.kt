package com.mymeetings.pairpomodoro.view

import android.content.Context
import android.content.res.AssetFileDescriptor
import android.media.MediaPlayer


object SoundUtils {

    fun triggerAlarmSound(context: Context) {

        val afd: AssetFileDescriptor = context.assets.openFd("alarm_ring_short.ogg")
        val player = MediaPlayer()
        player.setDataSource(afd.fileDescriptor, afd.startOffset, afd.length)
        player.prepare()
        player.start()
    }
}