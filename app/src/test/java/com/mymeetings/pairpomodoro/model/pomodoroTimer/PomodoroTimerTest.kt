package com.mymeetings.pairpomodoro.model.pomodoroTimer

import com.mymeetings.pairpomodoro.model.PomoState
import com.mymeetings.pairpomodoro.model.PomodoroStatus
import com.mymeetings.pairpomodoro.model.pomodoroAlarm.TimerAlarm
import com.mymeetings.pairpomodoro.model.pomodoroAlarm.TimerAlarmType
import com.mymeetings.pairpomodoro.model.pomodoroPreference.TimerPreference
import io.mockk.*
import io.mockk.impl.annotations.MockK
import org.junit.Before
import org.junit.Test

class PomodoroTimerTest {

    private lateinit var pomodoroTimer: PomodoroTimer

    @MockK
    private lateinit var tickerRunner: TickerRunner
    @MockK
    private lateinit var timerPreference: TimerPreference
    @MockK
    private lateinit var timerAlarm: TimerAlarm
    @MockK
    private lateinit var timerUpdater: TimerUpdater

    private val focusTime = 3000L
    private val shortBreakTime = 1000L
    private val longBreakTime = 1000L
    private val shortBreakCount = 2

    @Before
    fun setup() {
        MockKAnnotations.init(this, relaxUnitFun = true)

        every { timerPreference.getFocusTime() } returns focusTime
        every { timerPreference.getShortBreakTime() } returns shortBreakTime
        every { timerPreference.getLongBreakTime() } returns longBreakTime
        every { timerPreference.getShortBreakCount() } returns shortBreakCount

        pomodoroTimer = PomodoroTimer(
            tickerRunner = tickerRunner,
            timerPreference = timerPreference,
            timerAlarm = timerAlarm,
            timerUpdater = timerUpdater
        )
    }

    @Test
    fun `start should update info first and followed by each tick in timerUpdater`() {

        val timePassed = 1000L

        pomodoroTimer.start()
        pomodoroTimer.tick(timePassed)

        val expectedFirstPomoStatus = PomodoroStatus(
            pomoState = PomoState.Focus,
            balanceTime = timerPreference.getFocusTime(),
            shortBreaksLeft = timerPreference.getShortBreakCount(),
            pause = false
        )
        val expectedSecondPomoState =
            expectedFirstPomoStatus.copy(
                balanceTime = timerPreference.getFocusTime() - timePassed
            )

        verifyOrder {
            timerUpdater.update(expectedFirstPomoStatus, true)
            tickerRunner.run(pomodoroTimer)
            timerUpdater.update(expectedSecondPomoState, false)
        }
        verify(exactly = 0) { timerAlarm.alarm(any()) }
    }

    @Test
    fun `start with tick should change pomostate to shortBreak when time is over and update info in timerUpdater`() {

        pomodoroTimer.start()
        pomodoroTimer.tick(focusTime)

        val expectedPomoStatus = PomodoroStatus(
            pomoState = PomoState.ShortBreak,
            balanceTime = timerPreference.getShortBreakTime(),
            shortBreaksLeft = timerPreference.getShortBreakCount() - 1,
            pause = true
        )
        verify(exactly = 1) { timerUpdater.update(expectedPomoStatus, true) }
        verify(exactly = 1) { timerAlarm.alarm(TimerAlarmType.ShortBreak) }
    }

    @Test
    fun `start with tick should change pomostate to LongBreak when time is over and update info in timerUpdater`() {

        pomodoroTimer.start()
        pomodoroTimer.tick(focusTime)
        pomodoroTimer.tick(shortBreakTime)

        pomodoroTimer.tick(focusTime)
        pomodoroTimer.tick(shortBreakTime)

        pomodoroTimer.tick(focusTime)

        val expectedPomoStatus = PomodoroStatus(
            pomoState = PomoState.LongBreak,
            balanceTime = timerPreference.getLongBreakTime(),
            shortBreaksLeft = timerPreference.getShortBreakCount(),
            pause = true
        )
        verify(exactly = 1) { timerUpdater.update(expectedPomoStatus, true) }
        verify(exactly = 1) { timerAlarm.alarm(TimerAlarmType.LongBreak) }
    }

    @Test
    fun `pause should cancel the ticker runner and update info with pause and current state`() {
        val timeToPass = 1000L

        pomodoroTimer.start()
        pomodoroTimer.tick(timeToPass)
        pomodoroTimer.pause()

        val expectedPomoStatusForStart = PomodoroStatus(
            pomoState = PomoState.Focus,
            balanceTime = timerPreference.getFocusTime(),
            shortBreaksLeft = timerPreference.getShortBreakCount(),
            pause = false
        )
        val expectedPomoStatusForPause = PomodoroStatus(
            pomoState = PomoState.Focus,
            balanceTime = timerPreference.getFocusTime() - timeToPass,
            shortBreaksLeft = timerPreference.getShortBreakCount(),
            pause = true
        )
        verifyOrder {
            timerUpdater.update(expectedPomoStatusForStart, true)
            tickerRunner.run(pomodoroTimer)
            tickerRunner.cancel()
            timerUpdater.update(expectedPomoStatusForPause, true)
        }
    }

    @Test
    fun `reset should cancel the ticker runner and update info with pause and reset state`() {
        val timeToPass = 1000L

        pomodoroTimer.start()
        pomodoroTimer.tick(timeToPass)
        pomodoroTimer.reset()

        val expectedPomoStatusForStart = PomodoroStatus(
            pomoState = PomoState.Focus,
            balanceTime = timerPreference.getFocusTime(),
            shortBreaksLeft = timerPreference.getShortBreakCount(),
            pause = false
        )
        val expectedPomoStatusForPause = PomodoroStatus(
            pomoState = PomoState.Focus,
            balanceTime = timerPreference.getFocusTime(),
            shortBreaksLeft = timerPreference.getShortBreakCount(),
            pause = true
        )
        verifyOrder {
            timerUpdater.update(expectedPomoStatusForStart, true)
            tickerRunner.run(pomodoroTimer)
            tickerRunner.cancel()
            timerUpdater.update(expectedPomoStatusForPause, true)
        }
    }

    @Test
    fun `sync should sync the state and start if state is not paused`() {
        val timeToPass = 1000L

        val pomoStatus = PomodoroStatus(
            pomoState = PomoState.ShortBreak,
            balanceTime = 10000,
            shortBreaksLeft = 2,
            pause = false
        )

        pomodoroTimer.sync(pomoStatus)
        pomodoroTimer.tick(timeToPass)

        verifyOrder {
            tickerRunner.run(pomodoroTimer)
            timerUpdater.update(
                pomoStatus.copy(
                    balanceTime = pomoStatus.balanceTime - timeToPass
                ), false
            )
        }
    }

    @Test
    fun `sync should sync the state and pause if state is paused`() {

        val pomoStatus = PomodoroStatus(
            pomoState = PomoState.ShortBreak,
            balanceTime = 10000,
            shortBreaksLeft = 2,
            pause = true
        )

        pomodoroTimer.sync(pomoStatus)

        verifyOrder {
            tickerRunner.cancel()
            timerUpdater.update(
                pomoStatus.copy(
                    balanceTime = pomoStatus.balanceTime
                ), false
            )
        }
    }

    @Test
    fun `close should just cancel the timer`() {

        pomodoroTimer.close()

        verifyAll {
            tickerRunner.cancel()
        }
    }
}