package com.mymeetings.pairpomodoro.model.pomodoroManager

import com.google.firebase.database.PropertyName
import com.mymeetings.pairpomodoro.model.PomodoroStatus

data class PomodoroStatusWithKey(
    @PropertyName("sign") val sign: String = "",
    @PropertyName("pomodoroStatus") val pomodoroStatus: PomodoroStatus = PomodoroStatus(),
    @PropertyName("updatedTime") val updatedTime: Long = 0
) {
    fun reCorrectedPomodoroStatus(currentTimeInMillis : Long): PomodoroStatus {

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