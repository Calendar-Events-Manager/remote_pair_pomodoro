package com.mymeetings.pairpomodoro.model.pomodoroManager

import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import com.mymeetings.pairpomodoro.FirebaseUtils
import com.mymeetings.pairpomodoro.model.PomodoroStatus
import com.mymeetings.pairpomodoro.model.timerAlarm.TimerAlarm
import com.mymeetings.pairpomodoro.model.timerPreference.DefaultTimerPreference

class SyncPomodoroManager(
    private val shareKey: String,
    private val timerAlarm: TimerAlarm,
    private val updateCallback: ((pomodoroStatus: PomodoroStatus) -> Unit)?
) : ValueEventListener {

    private val pomodoroManager =
        PomodoroManager(
            updateCallback
        )

    fun create() {
        FirebaseUtils.getInfoDBReference(shareKey).addValueEventListener(this)
    }

    fun getPomodoroManager() = pomodoroManager

    override fun onCancelled(ignore: DatabaseError) {
        //Not needed.
    }

    override fun onDataChange(p0: DataSnapshot) {
        if (p0.key == FirebaseUtils.INFO_REF_KEY) {
            p0.getValue(DefaultTimerPreference::class.java)?.let {
                pomodoroManager.create(it, timerAlarm)
                FirebaseUtils.getSyncDBReference(shareKey).addValueEventListener(this)
            }
        } else if (p0.key == FirebaseUtils.SYNC_REF_KEY) {
            p0.getValue(PomodoroStatus::class.java)?.let {
                pomodoroManager.sync(pomodoroStatus = it)
            }
        }
    }
}