package com.mymeetings.pairpomodoro.model.pomodoroManager

import com.mymeetings.pairpomodoro.FirebaseUtils
import com.mymeetings.pairpomodoro.Utils
import com.mymeetings.pairpomodoro.model.PomodoroStatus
import com.mymeetings.pairpomodoro.model.timerAlarm.TimerAlarm
import com.mymeetings.pairpomodoro.model.timerPreference.DefaultTimerPreference
import java.util.concurrent.TimeUnit
import kotlin.math.abs

class SharePomodoroManager(
    private val updateCallback: ((pomodoroStatus: PomodoroStatus) -> Unit)?
) {

    private val shareKey = Utils.getRandomAlphaNumeric()
    private var lastUpdatedPomodoroStatus: PomodoroStatus? = null

    fun getShareKey() = shareKey

    fun create(
        timerPreference: DefaultTimerPreference,
        timerAlarm : TimerAlarm
    ): PomodoroManager {
        FirebaseUtils.getInfoDBReference(shareKey).setValue(timerPreference)
        return PomodoroManager(
            ::update
        ).also {
            it.create(timerPreference, timerAlarm)
        }
    }

    private fun update(pomodoroStatus: PomodoroStatus) {
        updateCallback?.invoke(pomodoroStatus)
        if (isChanged(pomodoroStatus)) {
            FirebaseUtils.getSyncDBReference(shareKey).setValue(pomodoroStatus)
        }
    }

    private fun isChanged(pomodoroStatus: PomodoroStatus): Boolean {

        val status = lastUpdatedPomodoroStatus?.let {
            it.pomoState != pomodoroStatus.pomoState
                    || it.pause != pomodoroStatus.pause
                    || it.shortBreaksLeft != pomodoroStatus.shortBreaksLeft
                    || abs(it.balanceTime - pomodoroStatus.balanceTime) > TimeUnit.SECONDS.toMillis(
                5
            )
        } ?: true

        if (status) {
            lastUpdatedPomodoroStatus = pomodoroStatus
        }

        return status
    }
}