package com.mymeetings.pairpomodoro.model.timer

import com.mymeetings.pairpomodoro.model.PomodoroStatus

interface TimerUpdater {

    fun update(pomodoroStatus: PomodoroStatus, actionChanges : Boolean)
}