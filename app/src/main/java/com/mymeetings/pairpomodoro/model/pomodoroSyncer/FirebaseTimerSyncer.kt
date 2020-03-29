package com.mymeetings.pairpomodoro.model.pomodoroSyncer

import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import com.mymeetings.pairpomodoro.model.PomodoroStatus
import com.mymeetings.pairpomodoro.model.pomodoroManager.PomoStatusWithKey
import com.mymeetings.pairpomodoro.model.pomodoroPreference.SyncableTimerPreference
import com.mymeetings.pairpomodoro.model.pomodoroPreference.TimerPreference
import com.mymeetings.pairpomodoro.model.pomodoroPreference.toSyncableTimerPreference
import com.mymeetings.pairpomodoro.utils.Utils

class FirebaseTimerSyncer : TimerSyncer, ValueEventListener {

    private var createdCallback: ((TimerPreference) -> Unit)? = null
    private var statusCallback: ((PomodoroStatus) -> Unit)? = null
    private var sharingKey: String? = null
    private val mySign: String = Utils.getRandomId()

    override fun registerTimerUpdate(
        sharingKey: String,
        createdCallback: ((TimerPreference) -> Unit)?,
        statusCallback: ((PomodoroStatus) -> Unit)?
    ) {
        this.sharingKey = sharingKey
        this.createdCallback = createdCallback
        this.statusCallback = statusCallback

        getInfoDBReference(sharingKey).addValueEventListener(this)
    }

    override fun unregisterTimerUpdate() {
        this.createdCallback = null
        this.statusCallback = null

        sharingKey?.let {
            getInfoDBReference(it).removeEventListener(this)
            getSyncDBReference(it).removeEventListener(this)
            getMasterDbReference(it).onDisconnect().removeValue()
        }
        sharingKey = null
    }

    override fun setTimerCreationInfo(timerPreference: TimerPreference) {
        sharingKey?.let {
            getInfoDBReference(it).setValue(timerPreference.toSyncableTimerPreference())
        }
    }

    override fun setTimerStatus(pomodoroStatus: PomodoroStatus) {
        sharingKey?.let {
            getSyncDBReference(it).setValue(
                PomoStatusWithKey(
                    sign = mySign,
                    pomodoroStatus = pomodoroStatus,
                    updatedTime = System.currentTimeMillis()
                )
            )
        }
    }

    override fun getSharingKey(): String {
        return sharingKey ?: ""
    }

    override fun onCancelled(ignore: DatabaseError) {
        //ignore
    }

    override fun onDataChange(datasnapshot: DataSnapshot) {
        if (datasnapshot.key == INFO_REF_KEY) {
            datasnapshot.getValue(SyncableTimerPreference::class.java)?.let { timerPreference ->
                sharingKey?.let {
                    getSyncDBReference(it).addValueEventListener(this)
                    createdCallback?.invoke(timerPreference)
                }
            }
        } else if (datasnapshot.key == SYNC_REF_KEY) {
            datasnapshot.getValue(PomoStatusWithKey::class.java)?.let { pomoStatusWithKey ->
                if (pomoStatusWithKey.sign != mySign) {
                    statusCallback?.invoke(pomoStatusWithKey.reCorrectedPomoStatus(System.currentTimeMillis()))
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