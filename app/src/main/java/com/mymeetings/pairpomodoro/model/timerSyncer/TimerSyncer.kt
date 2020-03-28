package com.mymeetings.pairpomodoro.model.timerSyncer

import com.mymeetings.pairpomodoro.model.PomodoroStatus
import com.mymeetings.pairpomodoro.model.timerPreference.TimerPreference

interface TimerSyncer {

    fun registerTimerUpdate(
        createdCallback: ((TimerPreference) -> Unit)? = null,
        statusCallback: ((PomodoroStatus) -> Unit)? = null
    )

    fun unregisterTimerUpdate()

    fun setTimerCreationInfo(timerPreference: TimerPreference)

    fun setTimerStatus(pomodoroStatus: PomodoroStatus)
}