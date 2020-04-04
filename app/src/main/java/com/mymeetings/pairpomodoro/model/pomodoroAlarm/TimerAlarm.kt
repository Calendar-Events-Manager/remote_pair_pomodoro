package com.mymeetings.pairpomodoro.model.pomodoroAlarm

import com.mymeetings.pairpomodoro.model.PomodoroState

interface TimerAlarm {

    fun alarm(timerAlarmType: TimerAlarmType)
}


fun PomodoroState.toTimerAlarmType(): TimerAlarmType {
    return when(this) {
        PomodoroState.LongBreak -> TimerAlarmType.LongBreak
        PomodoroState.Focus -> TimerAlarmType.Focus
        PomodoroState.ShortBreak -> TimerAlarmType.ShortBreak
    }
}