package com.mymeetings.pairpomodoro.model

data class PomodoroStatus(
    val pomoState: PomoState,
    val balanceTime: Long,
    val shortBreaksLeft: Int,
    val pause: Boolean
) {
    constructor() : this(PomoState.Focus, 0L, 0, true)
}