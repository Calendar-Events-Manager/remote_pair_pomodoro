package com.mymeetings.pairpomodoro.model.pomodoroManager

import com.mymeetings.pairpomodoro.model.PomodoroStatus

data class PomoStatusWithKey(
    val sign: String,
    val pomodoroStatus: PomodoroStatus,
    val updatedTime: Long
) {
    constructor() : this("", PomodoroStatus(), 0)

    fun reCorrectedPomoStatus(currentTimeInMillis : Long): PomodoroStatus {

        val reCorrectedBalanceTime = if (pomodoroStatus.pause) {
            pomodoroStatus.balanceTime
        } else {
            val diff = currentTimeInMillis - updatedTime
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