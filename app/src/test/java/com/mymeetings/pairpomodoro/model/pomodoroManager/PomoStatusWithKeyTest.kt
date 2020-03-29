package com.mymeetings.pairpomodoro.model.pomodoroManager

import com.mymeetings.pairpomodoro.model.PomoState
import com.mymeetings.pairpomodoro.model.PomodoroStatus
import org.junit.Test

class PomoStatusWithKeyTest {

    @Test
    fun `reCorrectedPomoStatus should give balance time as it is when pause is true`() {
        val pomoStatusWithKey = PomoStatusWithKey(
            sign = "abc",
            pomodoroStatus = PomodoroStatus(
                pomoState = PomoState.Focus,
                balanceTime = 5000,
                shortBreaksLeft = 2,
                pause = true
            ),
            updatedTime = 0L
        )

        val actualPomoStatus = pomoStatusWithKey.reCorrectedPomoStatus(6000)

        assert(actualPomoStatus.balanceTime == pomoStatusWithKey.pomodoroStatus.balanceTime)
    }

    @Test
    fun `reCorrectedPomoStatus should give adjusted balance time when pause is false`() {
        val updatedTime = 5000L
        val currentTime = 6000L
        val pomoStatusWithKey = PomoStatusWithKey(
            sign = "abc",
            pomodoroStatus = PomodoroStatus(
                pomoState = PomoState.Focus,
                balanceTime = 10000L,
                shortBreaksLeft = 2,
                pause = false
            ),
            updatedTime = updatedTime
        )

        val actualPomoStatus = pomoStatusWithKey.reCorrectedPomoStatus(currentTime)
        val expectedTime = pomoStatusWithKey.pomodoroStatus.balanceTime - (currentTime-updatedTime)

        assert(actualPomoStatus.balanceTime == expectedTime)
    }

    @Test
    fun `reCorrectedPomoStatus should give adjusted balance time as 0 when pause is false and calculated Balance time is less than 0`() {
        val updatedTime = 5000L
        val currentTime = 26000L
        val pomoStatusWithKey = PomoStatusWithKey(
            sign = "abc",
            pomodoroStatus = PomodoroStatus(
                pomoState = PomoState.Focus,
                balanceTime = 10000L,
                shortBreaksLeft = 2,
                pause = false
            ),
            updatedTime = updatedTime
        )

        val actualPomoStatus = pomoStatusWithKey.reCorrectedPomoStatus(currentTime)

        assert(actualPomoStatus.balanceTime == 0L)
    }
}