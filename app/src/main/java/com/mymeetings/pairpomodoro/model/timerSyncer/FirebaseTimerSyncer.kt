package com.mymeetings.pairpomodoro.model.timerSyncer

import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import com.mymeetings.pairpomodoro.model.PomodoroStatus
import com.mymeetings.pairpomodoro.model.pomodoroManager.PomoStatusWithKey
import com.mymeetings.pairpomodoro.model.timerPreference.SyncableTimerPreference
import com.mymeetings.pairpomodoro.model.timerPreference.TimerPreference
import com.mymeetings.pairpomodoro.model.timerPreference.toSyncableTimerPreference
import com.mymeetings.pairpomodoro.utils.Utils

class FirebaseTimerSyncer(
    private val sharingKey: String
) : TimerSyncer, ValueEventListener {

    private var createdCallback: ((TimerPreference) -> Unit)? = null
    private var statusCallback: ((PomodoroStatus) -> Unit)? = null
    private val mySign: String = Utils.getRandomId()

    override fun registerTimerUpdate(
        createdCallback: ((TimerPreference) -> Unit)?,
        statusCallback: ((PomodoroStatus) -> Unit)?
    ) {
        this.createdCallback = createdCallback
        this.statusCallback = statusCallback

        getInfoDBReference(sharingKey).addValueEventListener(this)
    }

    override fun unregisterTimerUpdate() {
        this.createdCallback = null
        this.statusCallback = null

        getInfoDBReference(sharingKey).removeEventListener(this)
        getSyncDBReference(sharingKey).removeEventListener(this)
        getMasterDbReference(sharingKey).onDisconnect().removeValue()
    }

    override fun setTimerCreationInfo(timerPreference: TimerPreference) {
        getInfoDBReference(sharingKey).setValue(timerPreference.toSyncableTimerPreference())
    }

    override fun setTimerStatus(pomodoroStatus: PomodoroStatus) {
        getSyncDBReference(sharingKey).setValue(
            PomoStatusWithKey(
                sign = mySign,
                pomodoroStatus = pomodoroStatus,
                updatedTime = System.currentTimeMillis()
            )
        )
    }

    override fun onCancelled(ignore: DatabaseError) {
        //ignore
    }

    override fun onDataChange(datasnapshot: DataSnapshot) {
        if (datasnapshot.key == INFO_REF_KEY) {
            datasnapshot.getValue(SyncableTimerPreference::class.java)?.let { timerPreference ->
                getSyncDBReference(sharingKey).addValueEventListener(this)
                createdCallback?.invoke(timerPreference)
            }
        } else if (datasnapshot.key == SYNC_REF_KEY) {
            datasnapshot.getValue(PomoStatusWithKey::class.java)?.let { pomoStatusWithKey ->
                if (pomoStatusWithKey.sign != mySign) {
                    statusCallback?.invoke(pomoStatusWithKey.reCorrectedPomoStatus())
                }
            }
        }
    }

    private fun getInfoDBReference(sharingKey: String) =
        Firebase.database.getReference(ROOT_REF_KEY).child(sharingKey).child(INFO_REF_KEY)

    private fun getSyncDBReference(sharingKey: String) =
        Firebase.database.getReference(ROOT_REF_KEY).child(sharingKey).child(SYNC_REF_KEY)

    private fun getMasterDbReference(sharingKey: String) =
        Firebase.database.getReference(ROOT_REF_KEY).child(sharingKey)

    companion object {

        private const val ROOT_REF_KEY = "timer"
        const val INFO_REF_KEY = "info"
        const val SYNC_REF_KEY = "sync"
    }
}