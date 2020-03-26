package com.mymeetings.pairpomodoro.model.timer

import com.mymeetings.pairpomodoro.model.PomoState
import com.mymeetings.pairpomodoro.model.PomodoroStatus
import com.mymeetings.pairpomodoro.model.timerAlarm.TimerAlarm
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
    private var pause: Boolean = true

    fun start() {
        pause = false
        tickerRunner.run(this)
    }

    fun pause() {
        pause = true
        tickerRunner.cancel()
        updateTimerInfo()
    }

    fun reset() {
        pause = true
        tickerRunner.cancel()
        updateTimerInfo()
    }

    fun sync(pomodoroStatus: PomodoroStatus) {
        this.balanceTime = pomodoroStatus.balanceTime
        this.pomoState = pomodoroStatus.pomoState
        this.shortBreaksLeft = pomodoroStatus.shortBreaksLeft
        if (pause) {
            pause()
        } else {
            start()
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
            PomoState.ShortBreak -> {
                timerAlarm.alarmForShortBreak()
            }
            PomoState.LongBreak -> {
                timerAlarm.alarmForLongBreak()
            }
            PomoState.Focus -> {
                timerAlarm.alarmForBreakOver()
            }
        }
    }

    private fun nextPomoState() =
        when (pomoState) {
            PomoState.Default -> PomoState.Focus
            PomoState.Focus -> {
                if (shortBreaksLeft > 0) {
                    shortBreaksLeft -= 1
                    balanceTime = timerPreference.getShortBreakTime()
                    PomoState.ShortBreak
                } else {
                    shortBreaksLeft = timerPreference.getShortBreakCount()
                    PomoState.LongBreak
                }
            }
            PomoState.ShortBreak -> PomoState.Focus
            PomoState.LongBreak -> PomoState.Focus
        }

    private fun updateTimerInfo() =
        timerUpdater.update(
            PomodoroStatus(
                pomoState = pomoState,
                balanceTime = balanceTime,
                shortBreaksLeft = shortBreaksLeft,
                pause = pause
            )
        )

}