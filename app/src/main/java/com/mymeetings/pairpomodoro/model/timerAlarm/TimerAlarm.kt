package com.mymeetings.pairpomodoro.model.timerAlarm

interface TimerAlarm {

    fun alarmForShortBreak()

    fun alarmForLongBreak()

    fun alarmForBreakOver()

    fun stopAlarm()
}