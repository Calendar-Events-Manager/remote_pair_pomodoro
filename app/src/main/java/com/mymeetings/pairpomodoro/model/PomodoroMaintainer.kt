package com.mymeetings.pairpomodoro.model

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.mymeetings.pairpomodoro.model.pomodoroManager.PomodoroCreationMode
import com.mymeetings.pairpomodoro.model.pomodoroManager.PomodoroManager
import com.mymeetings.pairpomodoro.model.pomodoroAlarm.TimerAlarm
import com.mymeetings.pairpomodoro.model.pomodoroPreference.TimerPreference

object PomodoroMaintainer {

    private var pomodoroStatusLiveData: MutableLiveData<PomodoroStatus> = MutableLiveData()
    private var pomodoroManager: PomodoroManager? = null

    fun createOwnPomodoro(
        timerAlarm: TimerAlarm,
        timerPreference: TimerPreference
    ) {
        pomodoroManager = PomodoroManager(
            timerAlarm = timerAlarm,
            timerPreference = timerPreference,
            updateCallback = ::update
        ).also {
            it.create()
        }
    }

    fun syncPomodoro(
        shareKey: String,
        timerAlarm: TimerAlarm
    ) {
        pomodoroManager = PomodoroManager(
            timerAlarm = timerAlarm,
            shareKey = shareKey,
            pomodoroCreationMode = PomodoroCreationMode.SYNC,
            updateCallback = ::update
        ).also {
            it.create()
        }
    }

    fun start() {
        pomodoroManager?.start()
    }

    fun pause() {
        pomodoroManager?.pause()
    }

    fun reset() {
        pomodoroManager?.reset()
    }

    fun clear() {
        pomodoroManager?.close()
        pomodoroStatusLiveData.postValue(null)
    }

    fun getPomodoroStatusLiveData(): LiveData<PomodoroStatus> = pomodoroStatusLiveData
    fun shareKey() = pomodoroManager?.getShareKey() ?: ""

    private fun update(pomodoroStatus: PomodoroStatus) {
        pomodoroStatusLiveData.postValue(pomodoroStatus)
    }

}