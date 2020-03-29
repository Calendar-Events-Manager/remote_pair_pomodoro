package com.mymeetings.pairpomodoro.model.pomodoroManager

import com.mymeetings.pairpomodoro.model.PomodoroStatus

data class PomoStatusWithKey(
    val sign: String,
    val pomodoroStatus: PomodoroStatus,
    val updatedTime: Long
) {
    constructor() : this("", PomodoroStatus(), 0)

    fun reCorrectedPomoStatus(): PomodoroStatus {

        val reCorrectedBalanceTime = if (pomodoroStatus.pause) {
            pomodoroStatus.balanceTime
        } else {
            val diff = System.currentTimeMillis() - updatedTime
            val adjustedBalanceTime = pomodoroStatus.balanceTime - diff
            if (adjustedBalanceTime > 0) {
                adjustedBalanceTime
            } else {
                0
            }
        }
        return pomodoroStatus.copy(
            balanceTime = reCorrectedBalanceTime
        )
    }
}