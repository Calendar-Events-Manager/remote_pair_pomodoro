package com.mymeetings.pairpomodoro.model.timer

import com.mymeetings.pairpomodoro.model.PomoState
import com.mymeetings.pairpomodoro.model.TimerAlarm
import com.mymeetings.pairpomodoro.model.timerPreference.TimerPreference
import java.util.concurrent.TimeUnit

class PomodoroTimer(
    private val tickerRunner: TickerRunner,
    private val timerPreference: TimerPreference,
    private val timerAlarm: TimerAlarm,
    private val timerUpdater: TimerUpdater
) : Ticker {

    private var pomoState: PomoState = PomoState.Default
    private var balanceTime: Long = timerPreference.getFocusTime()
    private var shortBreaksLeft: Int = timerPreference.getShortBreakCount()

    fun start() {
        tickerRunner.run(this)
    }

    fun pause() {
        tickerRunner.cancel()
        updateTimerInfo()
    }

    fun reset() {
        tickerRunner.cancel()
        updateTimerInfo()
    }

    fun sync(
        balanceTime: Long,
        pomoState: PomoState,
        pause: Boolean
    ) {
        this.balanceTime = balanceTime
        this.pomoState = pomoState
        if (pause) {
            tickerRunner.cancel()
        } else {
            tickerRunner.run(this)
        }
    }

    override fun tick() {
        balanceTime -= TimeUnit.SECONDS.toMillis(1)

        if (isTimerOver()) {
            pomoState = nextPomoState()
            pause()
            sendAlarmForNextPomoState()
        }
        updateTimerInfo()
    }

    private fun isTimerOver() = balanceTime <= 0

    private fun sendAlarmForNextPomoState() {
        when (pomoState) {
            is PomoState.ShortBreak -> {
                timerAlarm.alarmForShortBreak()
            }
            is PomoState.LongBreak -> {
                timerAlarm.alarmForLongBreak()
            }
            is PomoState.Focus -> {
                timerAlarm.alarmForBreakOver()
            }
        }
    }

    private fun nextPomoState() =
        when (pomoState) {
            is PomoState.Default -> PomoState.Focus
            is PomoState.Focus -> {
                if (shortBreaksLeft > 0) {
                    shortBreaksLeft -= 1
                    balanceTime = timerPreference.getShortBreakTime()
                    PomoState.ShortBreak
                } else {
                    shortBreaksLeft = timerPreference.getShortBreakCount()
                    PomoState.LongBreak
                }
            }
            is PomoState.ShortBreak -> PomoState.Focus
            is PomoState.LongBreak -> PomoState.Focus
        }

    private fun updateTimerInfo() =
        timerUpdater.update(
            pomoState = pomoState,
            balanceTime = balanceTime,
            shortBreaksLeft = shortBreaksLeft
        )

}