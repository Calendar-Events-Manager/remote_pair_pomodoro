package com.mymeetings.pairpomodoro.model.pomodoroTimer

import com.mymeetings.pairpomodoro.model.PomodoroState
import com.mymeetings.pairpomodoro.model.PomodoroStatus
import com.mymeetings.pairpomodoro.model.pomodoroAlarm.TimerAlarm
import com.mymeetings.pairpomodoro.model.pomodoroAlarm.toTimerAlarmType
import com.mymeetings.pairpomodoro.model.pomodoroPreference.TimerPreference

class PomodoroTimer(
    private val tickerRunner: TickerRunner,
    val timerPreference: TimerPreference,
    private val timerAlarm: TimerAlarm,
    private val timerUpdater: TimerUpdater
) : Ticker {

    private var pomodoroState: PomodoroState = PomodoroState.Focus
    private var balanceTime: Long = timerPreference.getFocusTime()
    private var shortBreaksLeft: Int = timerPreference.getShortBreakCount()
    private var pause: Boolean = true

    fun start() {
        pause = false
        updateTimerInfo(true)
        tickerRunner.run(this)
    }

    fun pause() {
        pause = true
        tickerRunner.cancel()
        updateTimerInfo(true)
    }

    fun reset() {
        pause = true
        tickerRunner.cancel()
        pomodoroState = PomodoroState.Focus
        balanceTime = timerPreference.getFocusTime()
        shortBreaksLeft = timerPreference.getShortBreakCount()
        updateTimerInfo(true)
    }

    fun close() {
        tickerRunner.cancel()
    }

    fun sync(pomodoroStatus: PomodoroStatus) {
        this.balanceTime = pomodoroStatus.balanceTime
        this.pomodoroState = pomodoroStatus.pomodoroState
        this.shortBreaksLeft = pomodoroStatus.shortBreaksLeft
        this.pause = pomodoroStatus.pause
        if (pomodoroStatus.pause) {
            tickerRunner.cancel()
        } else {
            tickerRunner.run(this)
        }
        updateTimerInfo(false)
    }

    override fun tick(elapsedTime: Long) {
        balanceTime -= elapsedTime

        if (isTimerOver()) {
            pomodoroState = nextPomodoroState()
            sendAlarmForNextPomodoroState()
            pause()
        } else {
            updateTimerInfo(false)
        }
    }

    private fun isTimerOver() = balanceTime <= 0

    private fun sendAlarmForNextPomodoroState() {
        timerAlarm.alarm(pomodoroState.toTimerAlarmType())
    }

    private fun nextPomodoroState() =
        when (pomodoroState) {
            PomodoroState.Focus -> {
                if (shortBreaksLeft > 0) {
                    shortBreaksLeft -= 1
                    balanceTime = timerPreference.getShortBreakTime()
                    PomodoroState.ShortBreak
                } else {
                    balanceTime = timerPreference.getLongBreakTime()
                    shortBreaksLeft = timerPreference.getShortBreakCount()
                    PomodoroState.LongBreak
                }
            }
            PomodoroState.ShortBreak -> {
                balanceTime = timerPreference.getFocusTime()
                PomodoroState.Focus
            }
            PomodoroState.LongBreak -> {
                balanceTime = timerPreference.getFocusTime()
                PomodoroState.Focus
            }
        }

    private fun updateTimerInfo(actionChanges: Boolean) =
        timerUpdater.update(
            pomodoroStatus = PomodoroStatus(
                pomodoroState = pomodoroState,
                balanceTime = balanceTime,
                shortBreaksLeft = shortBreaksLeft,
                pause = pause
            ),
            actionChanges = actionChanges
        )

}
