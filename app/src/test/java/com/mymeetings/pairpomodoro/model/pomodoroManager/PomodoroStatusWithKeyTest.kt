package com.mymeetings.pairpomodoro.model.pomodoroManager

import com.mymeetings.pairpomodoro.model.PomodoroState
import com.mymeetings.pairpomodoro.model.PomodoroStatus
import org.junit.Test

class PomodoroStatusWithKeyTest {

    @Test
    fun `reCorrectedPomodoroStatus should give balance time as it is when pause is true`() {
        val pomodoroStatusWithKey = PomodoroStatusWithKey(
            sign = "abc",
            pomodoroStatus = PomodoroStatus(
                pomodoroState = PomodoroState.Focus,
                balanceTime = 5000,
                shortBreaksLeft = 2,
                pause = true
            ),
            updatedTime = 0L
        )

        val actualPomodoroStatus = pomodoroStatusWithKey.reCorrectedPomodoroStatus(6000)

        assert(actualPomodoroStatus.balanceTime == pomodoroStatusWithKey.pomodoroStatus.balanceTime)
    }

    @Test
    fun `reCorrectedPomodoroStatus should give adjusted balance time when pause is false`() {
        val updatedTime = 5000L
        val currentTime = 6000L
        val pomodoroStatusWithKey = PomodoroStatusWithKey(
            sign = "abc",
            pomodoroStatus = PomodoroStatus(
                pomodoroState = PomodoroState.Focus,
                balanceTime = 10000L,
                shortBreaksLeft = 2,
                pause = false
            ),
            updatedTime = updatedTime
        )

        val actualPomodoroStatus = pomodoroStatusWithKey.reCorrectedPomodoroStatus(currentTime)
        val expectedTime = pomodoroStatusWithKey.pomodoroStatus.balanceTime - (currentTime-updatedTime)

        assert(actualPomodoroStatus.balanceTime == expectedTime)
    }

    @Test
    fun `reCorrectedPomodoroStatus should give adjusted balance time as 0 when pause is false and calculated Balance time is less than 0`() {
        val updatedTime = 5000L
        val currentTime = 26000L
        val pomodoroStatusWithKey = PomodoroStatusWithKey(
            sign = "abc",
            pomodoroStatus = PomodoroStatus(
                pomodoroState = PomodoroState.Focus,
                balanceTime = 10000L,
                shortBreaksLeft = 2,
                pause = false
            ),
            updatedTime = updatedTime
        )

        val actualPomodoroStatus = pomodoroStatusWithKey.reCorrectedPomodoroStatus(currentTime)

        assert(actualPomodoroStatus.balanceTime == 0L)
    }
}