package com.mymeetings.pairpomodoro.model

import android.os.Parcelable
import com.google.firebase.database.PropertyName
import kotlinx.android.parcel.Parcelize

@Parcelize
data class PomodoroStatus(
    @PropertyName("pomoState") val pomoState: PomoState,
    @PropertyName("balanceTime") val balanceTime: Long,
    @PropertyName("shortBreaksLeft") val shortBreaksLeft: Int,
    @PropertyName("pause") val pause: Boolean
) : Parcelable {
    constructor() : this(PomoState.Focus, 0L, 0, true)
}