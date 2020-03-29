package com.mymeetings.pairpomodoro.viewmodel

import androidx.lifecycle.ViewModel
import com.mymeetings.pairpomodoro.model.PomodoroMaintainer
import com.mymeetings.pairpomodoro.model.pomodoroAlarm.AndroidTimerAlarm
import com.mymeetings.pairpomodoro.model.pomodoroPreference.TimerPreference
import java.util.*

class PomodoroViewModel : ViewModel() {

    val pomodoroStatusLiveData = PomodoroMaintainer.getPomodoroStatusLiveData()
    val sharingKey get() = PomodoroMaintainer.shareKey()

    fun createOwnPomodoro(
        timerAlarm: AndroidTimerAlarm,
        timerPreference: TimerPreference
    ) {
        PomodoroMaintainer.createOwnPomodoro(
            timerAlarm = timerAlarm,
            timerPreference = timerPreference
        )
        PomodoroMaintainer.start()
    }

    fun syncPomodoro(shareKey: String, timerAlarm: AndroidTimerAlarm) {
        PomodoroMaintainer.syncPomodoro(shareKey.toUpperCase(Locale.getDefault()).trim(), timerAlarm)
    }

    fun pause() {
        PomodoroMaintainer.pause()
    }

    fun start() {
        PomodoroMaintainer.start()
    }

    fun reset() {
        PomodoroMaintainer.reset()
    }

    fun close() {
        PomodoroMaintainer.clear()
    }
}