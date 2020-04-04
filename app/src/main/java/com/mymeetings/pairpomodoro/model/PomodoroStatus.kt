package com.mymeetings.pairpomodoro.model

import android.os.Parcelable
import com.google.firebase.database.PropertyName
import kotlinx.android.parcel.Parcelize

@Parcelize
data class PomodoroStatus(
    @PropertyName("pomoState") val pomoState: PomoState = PomoState.Focus,
    @PropertyName("balanceTime") val balanceTime: Long = 0,
    @PropertyName("shortBreaksLeft") val shortBreaksLeft: Int = 0,
    @PropertyName("pause") val pause: Boolean = false
) : Parcelable