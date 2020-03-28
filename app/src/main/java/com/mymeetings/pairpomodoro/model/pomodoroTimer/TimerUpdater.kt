package com.mymeetings.pairpomodoro.model.pomodoroTimer

import com.mymeetings.pairpomodoro.model.PomodoroStatus

interface TimerUpdater {

    fun update(pomodoroStatus: PomodoroStatus, actionChanges : Boolean)
}