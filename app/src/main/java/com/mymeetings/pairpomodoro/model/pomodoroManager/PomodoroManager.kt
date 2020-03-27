package com.mymeetings.pairpomodoro.model.pomodoroManager

import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import com.mymeetings.pairpomodoro.model.PomodoroStatus
import com.mymeetings.pairpomodoro.model.timer.PomodoroTimer
import com.mymeetings.pairpomodoro.model.timer.TickerRunner
import com.mymeetings.pairpomodoro.model.timer.TimerUpdater
import com.mymeetings.pairpomodoro.model.timerAlarm.TimerAlarm
import com.mymeetings.pairpomodoro.model.timerPreference.DefaultTimerPreference
import com.mymeetings.pairpomodoro.model.timerPreference.TimerPreference
import com.mymeetings.pairpomodoro.utils.FirebaseUtils
import com.mymeetings.pairpomodoro.utils.Utils


class PomodoroManager(
    private val timerAlarm: TimerAlarm,
    private val shareKey: String = Utils.getRandomAlphaNumeric(),
    private val pomodoroCreationMode: PomodoroCreationMode = PomodoroCreationMode.CREATE,
    private val timerPreference: TimerPreference = DefaultTimerPreference(),
    private val updateCallback: ((pomodoroStatus: PomodoroStatus) -> Unit)? = null
) : TimerUpdater, ValueEventListener {

    private var pomodoroTimer: PomodoroTimer? = null
    private val mySign = Utils.getRandomId()

    fun create() {
        if (pomodoroCreationMode == PomodoroCreationMode.CREATE) {
            pomodoroTimer = PomodoroTimer(
                tickerRunner = TickerRunner(),
                timerPreference = timerPreference,
                timerAlarm = timerAlarm,
                timerUpdater = this
            ).also {
                it.start()
            }
            FirebaseUtils.getInfoDBReference(shareKey).setValue(timerPreference)
            FirebaseUtils.getSyncDBReference(shareKey).addValueEventListener(this)
        } else {
            FirebaseUtils.getInfoDBReference(shareKey).addValueEventListener(this)
        }
    }

    fun getShareKey() = shareKey

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
        FirebaseUtils.getInfoDBReference(shareKey).removeEventListener(this)
        FirebaseUtils.getSyncDBReference(shareKey).removeEventListener(this)
        FirebaseUtils.getMasterDbReference(shareKey).removeValue()
        pomodoroTimer?.close()
    }

    override fun update(pomodoroStatus: PomodoroStatus, actionChanges: Boolean) {
        updateCallback?.invoke(pomodoroStatus)
        if (actionChanges) {
            FirebaseUtils.getSyncDBReference(shareKey).setValue(
                PomoStatusWithKey(
                    sign = mySign,
                    pomodoroStatus = pomodoroStatus,
                    updatedTime = System.currentTimeMillis()
                )
            )
        }
    }

    override fun onCancelled(ignore: DatabaseError) {
        //ignore
    }

    override fun onDataChange(datasnapshot: DataSnapshot) {
        if (datasnapshot.key == FirebaseUtils.INFO_REF_KEY) {
            datasnapshot.getValue(DefaultTimerPreference::class.java)?.let { timerPreference ->
                pomodoroTimer = PomodoroTimer(
                    tickerRunner = TickerRunner(),
                    timerPreference = timerPreference,
                    timerAlarm = timerAlarm,
                    timerUpdater = this
                )
                FirebaseUtils.getSyncDBReference(shareKey).addValueEventListener(this)
            }
        } else if (datasnapshot.key == FirebaseUtils.SYNC_REF_KEY) {
            datasnapshot.getValue(PomoStatusWithKey::class.java)?.let {
                if (it.sign != mySign) {
                    val pomodoroStatus = it.reCorrectedPomoStatus()
                    pomodoroTimer?.sync(pomodoroStatus = pomodoroStatus)
                }
            }
        }
    }
}