package com.mymeetings.pairpomodoro.model.timerAlarm

import com.mymeetings.pairpomodoro.model.PomoState

interface TimerAlarm {

    fun alarm(timerAlarmType: TimerAlarmType)
}


fun PomoState.toTimerAlarmType(): TimerAlarmType {
    return when(this) {
        PomoState.LongBreak -> TimerAlarmType.LongBreak
        PomoState.Focus -> TimerAlarmType.Focus
        PomoState.ShortBreak -> TimerAlarmType.ShortBreak
    }
}