package com.mymeetings.pairpomodoro.model.timer

import com.mymeetings.pairpomodoro.model.PomoState

interface TimerUpdater {

    fun update(pomoState: PomoState, balanceTime : Long, shortBreaksLeft : Int)
}