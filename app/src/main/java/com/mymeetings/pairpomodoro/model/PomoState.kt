package com.mymeetings.pairpomodoro.model

sealed class PomoState {

    object Default : PomoState()
    object Focus : PomoState()
    object ShortBreak : PomoState()
    object LongBreak : PomoState()
}