package com.mymeetings.pairpomodoro

import java.util.*
import java.util.concurrent.TimeUnit


object Utils {

    fun getDurationBreakdown(diff: Long): String {
        var millis = diff
        if (millis < 0) {
            return "00:00"
        }
        val minutes: Long = TimeUnit.MILLISECONDS.toMinutes(millis)
        millis -= TimeUnit.MINUTES.toMillis(minutes)
        val seconds: Long = TimeUnit.MILLISECONDS.toSeconds(millis)
        return String.format(Locale.ENGLISH, "%02d:%02d", minutes, seconds)
    }

    fun getRandomAlphaNumeric(): String {
        val allowedChars = "ABCDEFGHIJKLMNOPRSTUV"
        return (1..5)
            .map { allowedChars.random() }
            .joinToString("")
    }
}