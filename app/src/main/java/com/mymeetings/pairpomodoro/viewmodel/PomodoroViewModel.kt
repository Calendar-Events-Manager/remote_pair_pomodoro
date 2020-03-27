package com.mymeetings.pairpomodoro.viewmodel

import androidx.lifecycle.ViewModel
import com.mymeetings.pairpomodoro.model.SingletonPomodoro
import com.mymeetings.pairpomodoro.model.timerAlarm.AndroidTimerAlarm

class PomodoroViewModel : ViewModel() {

    val pomodoroStatusLiveData = SingletonPomodoro.getPomodoroStatusLiveData()
    val sharingKey get() = SingletonPomodoro.shareKey
    val pomodoroMode get() = SingletonPomodoro.pomodoroMode

    fun createOwnPomodoro(timerAlarm: AndroidTimerAlarm) {
        SingletonPomodoro.createOwnPomodoro(timerAlarm)
    }

    fun syncPomodoro(shareKey: String, timerAlarm: AndroidTimerAlarm) {
        SingletonPomodoro.syncPomodoro(shareKey, timerAlarm)
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

    fun close() {
        SingletonPomodoro.clear()
    }
}