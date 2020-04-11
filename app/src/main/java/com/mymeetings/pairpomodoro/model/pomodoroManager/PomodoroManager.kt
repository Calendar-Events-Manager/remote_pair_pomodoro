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
    private val onTimerCreated: (TimerPreference, String) -> Unit,
    private val onStatusUpdated: (pomodoroStatus: PomodoroStatus) -> Unit
) : TimerUpdater {

    private var pomodoroTimer: PomodoroTimer? = null

    fun sync(
        sharingKey: String,
        onSyncFailed: () -> Unit
    ) {
        timerSyncer.registerTimerUpdate(
            sharingKey = sharingKey,
            createdCallback = ::onTimerSyncCreated,
            statusCallback = ::onTimerSyncUpdated,
            keyNotFoundCallback = onSyncFailed
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
        val sharingKey = Utils.getRandomAlphaNumeric()
        timerSyncer.registerTimerUpdate(
            sharingKey = sharingKey,
            statusCallback = ::onTimerSyncUpdated
        )
        timerSyncer.setTimerCreationInfo(timerPreference)
        onTimerCreated(timerPreference, sharingKey)
    }

    fun getShareKey() = timerSyncer.getSharingKey()

    fun start() {
        pomodoroTimer?.start()
    }

    fun pause() {
        pomodoroTimer?.pause()
    }

    fun isRunning() = pomodoroTimer != null

    fun reset() {
        pomodoroTimer?.reset()
    }

    fun close() {
        timerSyncer.unregisterTimerUpdate()
        pomodoroTimer?.close()
        pomodoroTimer = null
    }

    override fun update(pomodoroStatus: PomodoroStatus, actionChanges: Boolean) {
        onStatusUpdated.invoke(pomodoroStatus)
        if (actionChanges) {
            timerSyncer.setTimerStatus(pomodoroStatus)
        }
    }

    private fun onTimerSyncCreated(timerPreference: TimerPreference, sharingKey: String) {
        pomodoroTimer = PomodoroTimer(
            tickerRunner = TickerRunner(),
            timerPreference = timerPreference,
            timerAlarm = timerAlarm,
            timerUpdater = this
        )
        onTimerCreated(timerPreference, sharingKey)
    }

    private fun onTimerSyncUpdated(pomodoroStatus: PomodoroStatus) {
        pomodoroTimer?.sync(pomodoroStatus)
    }
}
