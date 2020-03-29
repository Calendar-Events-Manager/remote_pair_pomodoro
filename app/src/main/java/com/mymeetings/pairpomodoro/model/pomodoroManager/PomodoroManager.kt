package com.mymeetings.pairpomodoro.model.pomodoroManager

import com.mymeetings.pairpomodoro.model.PomodoroStatus
import com.mymeetings.pairpomodoro.model.pomodoroAlarm.TimerAlarm
import com.mymeetings.pairpomodoro.model.pomodoroPreference.TimerPreference
import com.mymeetings.pairpomodoro.model.pomodoroSyncer.TimerSyncer
import com.mymeetings.pairpomodoro.model.pomodoroTimer.PomodoroTimer
import com.mymeetings.pairpomodoro.model.pomodoroTimer.TickerRunner
import com.mymeetings.pairpomodoro.model.pomodoroTimer.TimerUpdater
import com.mymeetings.pairpomodoro.utils.Utils


class PomodoroManager(
    private val timerAlarm: TimerAlarm,
    private val timerSyncer: TimerSyncer,
    private val updateCallback: ((pomodoroStatus: PomodoroStatus) -> Unit)
) : TimerUpdater {

    private var pomodoroTimer: PomodoroTimer? = null

    fun sync(shareKey: String) {
        timerSyncer.registerTimerUpdate(
            sharingKey = shareKey,
            createdCallback = ::onTimerSyncCreate,
            statusCallback = ::onTimerSyncUpdate
        )
    }

    fun create(
        timerPreference: TimerPreference
    ) {
        pomodoroTimer = PomodoroTimer(
            tickerRunner = TickerRunner(),
            timerPreference = timerPreference,
            timerAlarm = timerAlarm,
            timerUpdater = this
        )
        timerSyncer.registerTimerUpdate(
            sharingKey = Utils.getRandomAlphaNumeric(),
            statusCallback = ::onTimerSyncUpdate
        )
        timerSyncer.setTimerCreationInfo(timerPreference)

    }

    fun getShareKey() = timerSyncer.getSharingKey()

    fun start() {
        pomodoroTimer?.start()
    }

    fun pause() {
        pomodoroTimer?.pause()
    }

    fun reset() {
        pomodoroTimer?.reset()
    }

    fun close() {
        timerSyncer.unregisterTimerUpdate()
        pomodoroTimer?.close()
    }

    override fun update(pomodoroStatus: PomodoroStatus, actionChanges: Boolean) {
        updateCallback.invoke(pomodoroStatus)
        if (actionChanges) {
            timerSyncer.setTimerStatus(pomodoroStatus)
        }
    }

    private fun onTimerSyncCreate(timerPreference: TimerPreference) {
        pomodoroTimer = PomodoroTimer(
            tickerRunner = TickerRunner(),
            timerPreference = timerPreference,
            timerAlarm = timerAlarm,
            timerUpdater = this
        )
    }

    private fun onTimerSyncUpdate(pomodoroStatus: PomodoroStatus) {
        pomodoroTimer?.sync(pomodoroStatus)
    }
}
