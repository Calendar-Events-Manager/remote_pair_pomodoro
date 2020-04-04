package com.mymeetings.pairpomodoro.viewmodel

import androidx.lifecycle.ViewModel
import com.mymeetings.pairpomodoro.model.PomodoroMaintainer

class PomodoroViewModel : ViewModel() {
    val pomodoroStatusLiveData = PomodoroMaintainer.getPomodoroStatusLiveData()
    val syncFailedLiveData = PomodoroMaintainer.getSyncFailedLiveData()
    val sharingKey get() = PomodoroMaintainer.shareKey()
}