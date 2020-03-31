package com.mymeetings.pairpomodoro.viewmodel

import androidx.lifecycle.ViewModel
import com.mymeetings.pairpomodoro.model.PomodoroMaintainer
import com.mymeetings.pairpomodoro.model.pomodoroAlarm.AndroidTimerAlarm
import com.mymeetings.pairpomodoro.model.pomodoroPreference.TimerPreference
import java.util.*

class PomodoroViewModel : ViewModel() {

    val pomodoroStatusLiveData = PomodoroMaintainer.getPomodoroStatusLiveData()
    val syncFailedLiveData = PomodoroMaintainer.getSyncFailedLiveData()
    val sharingKey get() = PomodoroMaintainer.shareKey()

    fun pause() {
        PomodoroMaintainer.pause()
    }

    fun start() {
        PomodoroMaintainer.start()
    }

    fun reset() {
        PomodoroMaintainer.reset()
    }
}