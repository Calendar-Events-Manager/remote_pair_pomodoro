package com.mymeetings.pairpomodoro.model.timer

import com.mymeetings.pairpomodoro.model.PomoState
import com.mymeetings.pairpomodoro.model.PomodoroStatus
import com.mymeetings.pairpomodoro.model.timerAlarm.TimerAlarm
import com.mymeetings.pairpomodoro.model.timerAlarm.TimerAlarmType
import com.mymeetings.pairpomodoro.model.timerPreference.TimerPreference
import io.mockk.*
import io.mockk.impl.annotations.MockK
import org.junit.Before
import org.junit.Test
import java.util.concurrent.TimeUnit

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

    @Before
    fun setup() {
        MockKAnnotations.init(this, relaxUnitFun = true)

        every { timerPreference.getFocusTime() } returns TimeUnit.SECONDS.toMillis(3)
        every { timerPreference.getShortBreakTime() } returns TimeUnit.SECONDS.toMillis(1)
        every { timerPreference.getLongBreakTime() } returns TimeUnit.SECONDS.toMillis(2)
        every { timerPreference.getShortBreakCount() } returns 2

        pomodoroTimer = PomodoroTimer(
            tickerRunner = tickerRunner,
            timerPreference = timerPreference,
            timerAlarm = timerAlarm,
            timerUpdater = timerUpdater
        )
    }

    @Test
    fun `start should update info first and followed by each tick in timerUpdater`() {

        every { tickerRunner.run(pomodoroTimer) } answers { pomodoroTimer.tick() }

        pomodoroTimer.start()

        val expectedFirstPomoStatus = PomodoroStatus(
            pomoState = PomoState.Focus,
            balanceTime = timerPreference.getFocusTime(),
            shortBreaksLeft = timerPreference.getShortBreakCount(),
            pause = false
        )
        val expectedSecondPomoState =
            expectedFirstPomoStatus.copy(
                balanceTime = timerPreference.getFocusTime() - TimeUnit.SECONDS.toMillis(
                    1
                )
            )

        verifyOrder {
            timerUpdater.update(expectedFirstPomoStatus, true)
            timerUpdater.update(expectedSecondPomoState, false)
        }
        verify(exactly = 0) { timerAlarm.alarm(any()) }
    }

    @Test
    fun `start with tick should change pomostate to shortBreak when time is over and update info in timerUpdater`() {

        every { tickerRunner.run(pomodoroTimer) } answers {
            pomodoroTimer.tick() //Focus
            pomodoroTimer.tick() //Focus
            pomodoroTimer.tick() //Focus. -> ShortBreak
        }
        every { timerPreference.getFocusTime() } returns TimeUnit.SECONDS.toMillis(1)

        pomodoroTimer.start()

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

        every { tickerRunner.run((pomodoroTimer)) } answers {
            pomodoroTimer.tick() //Focus
            pomodoroTimer.tick() //Focus
            pomodoroTimer.tick() //Focus -> ShortBreak
            pomodoroTimer.tick() //ShortBreak -> Focus
            pomodoroTimer.tick() //Focus
            pomodoroTimer.tick() //Focus
            pomodoroTimer.tick() //Focus -> ShortBreak
            pomodoroTimer.tick() //ShortBreak -> Focus
            pomodoroTimer.tick() //Focus
            pomodoroTimer.tick() //Focus
            pomodoroTimer.tick() //Focus
        }
        every { timerPreference.getFocusTime() } returns TimeUnit.SECONDS.toMillis(1)

        pomodoroTimer.start()

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
        every { tickerRunner.run(pomodoroTimer) } answers {
            pomodoroTimer.tick() //Focus
            pomodoroTimer.tick() //Focus
            pomodoroTimer.tick() //Focus. -> ShortBreak
        }
        every { timerPreference.getFocusTime() } returns TimeUnit.SECONDS.toMillis(1)

        pomodoroTimer.start()
        pomodoroTimer.pause()

        val expectedPomoStatus = PomodoroStatus(
            pomoState = PomoState.ShortBreak,
            balanceTime = timerPreference.getShortBreakTime(),
            shortBreaksLeft = timerPreference.getShortBreakCount() - 1,
            pause = true
        )
        verifyOrder {
            tickerRunner.cancel()
            timerUpdater.update(expectedPomoStatus, true)
        }

    }

    @Test
    fun `reset should cancel the ticker runner and update info with pause and reset state`() {
        every { tickerRunner.run(pomodoroTimer) } answers {
            pomodoroTimer.tick() //Focus
            pomodoroTimer.tick() //Focus
            pomodoroTimer.tick() //Focus. -> ShortBreak
        }
        every { timerPreference.getFocusTime() } returns TimeUnit.SECONDS.toMillis(1)

        pomodoroTimer.start()
        pomodoroTimer.reset()

        val expectedPomoStatus = PomodoroStatus(
            pomoState = PomoState.Focus,
            balanceTime = timerPreference.getFocusTime(),
            shortBreaksLeft = timerPreference.getShortBreakCount(),
            pause = true
        )
        verifyOrder {
            tickerRunner.cancel()
            timerUpdater.update(expectedPomoStatus, true)
        }
    }

    @Test
    fun `sync should sync the state and start if state is not paused`() {

        every { tickerRunner.run(pomodoroTimer) } answers { pomodoroTimer.tick() }
        val pomoStatus = PomodoroStatus(
            pomoState = PomoState.ShortBreak,
            balanceTime = 10000,
            shortBreaksLeft = 2,
            pause = false
        )

        pomodoroTimer.sync(pomoStatus)

        verifyOrder {
            tickerRunner.run(pomodoroTimer)
            timerUpdater.update(
                pomoStatus.copy(
                    balanceTime = pomoStatus.balanceTime - TimeUnit.SECONDS.toMillis(
                        1
                    )
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