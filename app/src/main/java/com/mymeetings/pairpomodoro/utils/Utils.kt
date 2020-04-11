package com.mymeetings.pairpomodoro.utils

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

    fun getPercentageValue(maxValue: Long, progressValue: Long) =
        100.minus(
            ((maxValue.minus(progressValue).toDouble()).div(maxValue)).times(
                100
            )
        ).toInt()


    fun getRandomAlphaNumeric(): String {
        val allowedChars = "ABCDEFGHIJKLMNOPRSTUV"
        return getRandomFrom(allowedChars, 5)
    }

    fun getRandomId(): String {
        val allowedChars = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz"
        return getRandomFrom(allowedChars, 6)
    }

    private fun getRandomFrom(allowedChars: String, length: Int) = (1..length)
        .map { allowedChars.random() }
        .joinToString("")

}