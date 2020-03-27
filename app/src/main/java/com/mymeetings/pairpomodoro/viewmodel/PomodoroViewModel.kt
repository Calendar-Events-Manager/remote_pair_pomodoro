package com.mymeetings.pairpomodoro.viewmodel

import androidx.lifecycle.ViewModel
import com.mymeetings.pairpomodoro.model.SingletonPomodoro
import com.mymeetings.pairpomodoro.model.timerAlarm.AndroidTimerAlarm
import com.mymeetings.pairpomodoro.model.timerPreference.TimerPreference
import java.util.*

class PomodoroViewModel : ViewModel() {

    val pomodoroStatusLiveData = SingletonPomodoro.getPomodoroStatusLiveData()
    val sharingKey get() = SingletonPomodoro.shareKey()

    fun createOwnPomodoro(
        timerAlarm: AndroidTimerAlarm,
        timerPreference: TimerPreference
    ) {
        SingletonPomodoro.createOwnPomodoro(
            timerAlarm = timerAlarm,
            timerPreference = timerPreference
        )
    }

    fun syncPomodoro(shareKey: String, timerAlarm: AndroidTimerAlarm) {
        SingletonPomodoro.syncPomodoro(shareKey.toUpperCase(Locale.getDefault()).trim(), timerAlarm)
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