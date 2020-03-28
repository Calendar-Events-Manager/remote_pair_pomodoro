package com.mymeetings.pairpomodoro.model.pomodoroSyncer

import com.mymeetings.pairpomodoro.model.PomodoroStatus
import com.mymeetings.pairpomodoro.model.pomodoroPreference.TimerPreference

interface TimerSyncer {

    fun registerTimerUpdate(
        createdCallback: ((TimerPreference) -> Unit)? = null,
        statusCallback: ((PomodoroStatus) -> Unit)? = null
    )

    fun unregisterTimerUpdate()

    fun setTimerCreationInfo(timerPreference: TimerPreference)

    fun setTimerStatus(pomodoroStatus: PomodoroStatus)
}