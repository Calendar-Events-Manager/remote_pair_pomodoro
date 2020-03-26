package com.mymeetings.pairpomodoro.viewmodel

import androidx.lifecycle.ViewModel
import com.mymeetings.pairpomodoro.model.SingletonPomodoro

class PomodoroViewModel : ViewModel() {

    val pomodoroStatusLiveData = SingletonPomodoro.getPomodoroStatusLiveData()
    val sharingKey get() = SingletonPomodoro.shareKey
    val pomodoroMode get() = SingletonPomodoro.pomodoroMode

    fun createOwnPomodoro() {
        SingletonPomodoro.createOwnPomodoro()
    }

    fun syncPomodoro(shareKey: String) {
        SingletonPomodoro.syncPomodoro(shareKey)
    }

    fun pause() {
        SingletonPomodoro.pause()
    }

    fun start() {
        SingletonPomodoro.start()
    }

    fun reset() {
        SingletonPomodoro.reset()
    }
}