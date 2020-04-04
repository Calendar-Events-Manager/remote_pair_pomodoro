package com.mymeetings.pairpomodoro.model

import SingleLiveEvent
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.mymeetings.pairpomodoro.model.pomodoroAlarm.TimerAlarm
import com.mymeetings.pairpomodoro.model.pomodoroManager.PomodoroManager
import com.mymeetings.pairpomodoro.model.pomodoroPreference.TimerPreference
import com.mymeetings.pairpomodoro.model.pomodoroSyncer.FirebaseTimerSyncer
import com.mymeetings.pairpomodoro.model.pomodoroSyncer.TimerSyncer

class PomodoroMaintainer {

    private val pomodoroStatusLiveData: MutableLiveData<PomodoroStatus> = MutableLiveData()
    private val syncFailedEvent: SingleLiveEvent<Unit> = SingleLiveEvent()

    private var pomodoroManager: PomodoroManager? = null

    fun createOwnPomodoro(
        timerAlarm: TimerAlarm,
        timerPreference: TimerPreference
    ) {
        pomodoroManager = PomodoroManager(
            timerAlarm = timerAlarm,
            timerSyncer = FirebaseTimerSyncer(),
            updateCallback = ::update
        ).also {
            it.create(timerPreference)
        }
    }

    fun syncPomodoro(
        shareKey: String,
        timerAlarm: TimerAlarm
    ) {
        pomodoroManager = PomodoroManager(
            timerAlarm = timerAlarm,
            timerSyncer = FirebaseTimerSyncer(),
            updateCallback = ::update
        ).also {
            it.sync(
                shareKey = shareKey,
                syncFailedCallback = ::syncFailed
            )
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
    fun getSyncFailedLiveData(): LiveData<Unit> = syncFailedEvent
    fun shareKey() = pomodoroManager?.getShareKey() ?: ""

    private fun update(pomodoroStatus: PomodoroStatus) {
        pomodoroStatusLiveData.postValue(pomodoroStatus)
    }

    private fun syncFailed() {
        syncFailedEvent.value = Unit
    }

    fun isRunning() {

    }

}