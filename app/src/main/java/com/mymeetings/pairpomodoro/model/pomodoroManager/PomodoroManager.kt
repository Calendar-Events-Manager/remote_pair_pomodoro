package com.mymeetings.pairpomodoro.model.pomodoroManager

import com.mymeetings.pairpomodoro.model.PomodoroStatus
import com.mymeetings.pairpomodoro.model.timer.PomodoroTimer
import com.mymeetings.pairpomodoro.model.timer.TickerRunner
import com.mymeetings.pairpomodoro.model.timer.TimerUpdater
import com.mymeetings.pairpomodoro.model.timerAlarm.TimerAlarm
import com.mymeetings.pairpomodoro.model.timerPreference.DefaultTimerPreference


class PomodoroManager(
    private val updateCallback: ((pomodoroStatus: PomodoroStatus) -> Unit)?
) : TimerUpdater {

    private lateinit var pomodoroTimer: PomodoroTimer

    internal fun create(
        timerPreference: DefaultTimerPreference,
        timerAlarm: TimerAlarm
    ) {
        pomodoroTimer = PomodoroTimer(
            tickerRunner = TickerRunner(),
            timerPreference = timerPreference,
            timerAlarm = timerAlarm,
            timerUpdater = this
        )
        pomodoroTimer.start()
    }

    fun start() {
        pomodoroTimer.start()
    }

    fun pause() {
        pomodoroTimer.pause()
    }

    fun reset() {
        pomodoroTimer.reset()
    }

    fun sync(pomodoroStatus: PomodoroStatus) {
        pomodoroTimer.sync(pomodoroStatus)
    }

    override fun update(pomodoroStatus: PomodoroStatus) {
        updateCallback?.invoke(pomodoroStatus)
    }
}