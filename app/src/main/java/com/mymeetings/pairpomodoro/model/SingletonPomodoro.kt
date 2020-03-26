package com.mymeetings.pairpomodoro.model

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.mymeetings.pairpomodoro.model.pomodoroManager.PomodoroManager
import com.mymeetings.pairpomodoro.model.pomodoroManager.SharePomodoroManager
import com.mymeetings.pairpomodoro.model.pomodoroManager.SyncPomodoroManager
import com.mymeetings.pairpomodoro.model.timerPreference.DefaultTimerPreference

object SingletonPomodoro {

    private var pomodoroStatusLiveData: MutableLiveData<PomodoroStatus> = MutableLiveData()
    var shareKey: String = ""
    var pomodoroMode = PomodoroMode.CREATED

    private lateinit var pomodoroManager: PomodoroManager

    fun createOwnPomodoro() {
        pomodoroMode = PomodoroMode.CREATED
        pomodoroManager = SharePomodoroManager(
            ::update
        ).also {
            this.shareKey = it.getShareKey()
        }.create(DefaultTimerPreference())
    }

    fun syncPomodoro(shareKey: String) {
        pomodoroMode = PomodoroMode.SYNCED
        this.shareKey = shareKey
        pomodoroManager = SyncPomodoroManager(
            shareKey,
            ::update
        ).also {
            it.create()
        }.getPomodoroManager()
    }

    fun start() {
        pomodoroManager.start()
    }

    fun pause() {
        pomodoroManager.pause()
    }

    fun reset() {
        pomodoroManager.reset()
    }

    fun getPomodoroStatusLiveData(): LiveData<PomodoroStatus> = pomodoroStatusLiveData

    private fun update(pomodoroStatus: PomodoroStatus) {
        pomodoroStatusLiveData.postValue(pomodoroStatus)
    }
}