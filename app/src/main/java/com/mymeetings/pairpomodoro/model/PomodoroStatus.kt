package com.mymeetings.pairpomodoro.model

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize

@Parcelize
data class PomodoroStatus(
    val pomoState: PomoState,
    val balanceTime: Long,
    val shortBreaksLeft: Int,
    val pause: Boolean
) : Parcelable {
    constructor() : this(PomoState.Focus, 0L, 0, true)
}