package com.mymeetings.pairpomodoro.model.timer

import com.mymeetings.pairpomodoro.model.PomoState
import com.mymeetings.pairpomodoro.model.PomodoroStatus
import com.mymeetings.pairpomodoro.model.timerAlarm.TimerAlarm
import com.mymeetings.pairpomodoro.model.timerAlarm.toTimerAlarmType
import com.mymeetings.pairpomodoro.model.timerPreference.TimerPreference
import java.util.concurrent.TimeUnit

class PomodoroTimer(
    private val tickerRunner: TickerRunner,
    private val timerPreference: TimerPreference,
    private val timerAlarm: TimerAlarm,
    private val timerUpdater: TimerUpdater
) : Ticker {

    private var pomoState: PomoState = PomoState.Focus
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
        pomoState = PomoState.Focus
        balanceTime = timerPreference.getFocusTime()
        shortBreaksLeft = timerPreference.getShortBreakCount()
        updateTimerInfo(true)
    }

    fun close() {
        tickerRunner.cancel()
    }

    fun sync(pomodoroStatus: PomodoroStatus) {
        this.balanceTime = pomodoroStatus.balanceTime
        this.pomoState = pomodoroStatus.pomoState
        this.shortBreaksLeft = pomodoroStatus.shortBreaksLeft
        this.pause = pomodoroStatus.pause
        if (pomodoroStatus.pause) {
            tickerRunner.cancel()
        } else {
            tickerRunner.run(this)
        }
        updateTimerInfo(false)
    }

    override fun tick() {
        balanceTime -= TimeUnit.SECONDS.toMillis(1)

        if (isTimerOver()) {
            pomoState = nextPomoState()
            sendAlarmForNextPomoState()
            pause()
        } else {
            updateTimerInfo(false)
        }
    }

    private fun isTimerOver() = balanceTime <= 0

    private fun sendAlarmForNextPomoState() {
        timerAlarm.alarm(pomoState.toTimerAlarmType())
    }

    private fun nextPomoState() =
        when (pomoState) {
            PomoState.Focus -> {
                if (shortBreaksLeft > 0) {
                    shortBreaksLeft -= 1
                    balanceTime = timerPreference.getShortBreakTime()
                    PomoState.ShortBreak
                } else {
                    balanceTime = timerPreference.getLongBreakTime()
                    shortBreaksLeft = timerPreference.getShortBreakCount()
                    PomoState.LongBreak
                }
            }
            PomoState.ShortBreak -> {
                balanceTime = timerPreference.getFocusTime()
                PomoState.Focus
            }
            PomoState.LongBreak -> {
                balanceTime = timerPreference.getFocusTime()
                PomoState.Focus
            }
        }

    private fun updateTimerInfo(actionChanges: Boolean) =
        timerUpdater.update(
            pomodoroStatus = PomodoroStatus(
                pomoState = pomoState,
                balanceTime = balanceTime,
                shortBreaksLeft = shortBreaksLeft,
                pause = pause
            ),
            actionChanges = actionChanges
        )

}
