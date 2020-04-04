package com.mymeetings.pairpomodoro.model.pomodoroSyncer

import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import com.mymeetings.pairpomodoro.model.PomodoroStatus
import com.mymeetings.pairpomodoro.model.pomodoroManager.PomodoroStatusWithKey
import com.mymeetings.pairpomodoro.model.pomodoroPreference.SyncableTimerPreference
import com.mymeetings.pairpomodoro.model.pomodoroPreference.TimerPreference
import com.mymeetings.pairpomodoro.model.pomodoroPreference.toSyncableTimerPreference
import com.mymeetings.pairpomodoro.utils.Utils

class FirebaseTimerSyncer : TimerSyncer, ValueEventListener {

    private var createdCallback: ((TimerPreference) -> Unit)? = null
    private var statusCallback: ((PomodoroStatus) -> Unit)? = null
    private var keyNotFoundCallback: (() -> Unit)? = null
    private var sharingKey: String? = null
    private var mySign: String = Utils.getRandomId()

    override fun registerTimerUpdate(
        sharingKey: String,
        createdCallback: ((TimerPreference) -> Unit)?,
        statusCallback: ((PomodoroStatus) -> Unit)?,
        keyNotFoundCallback: (() -> Unit)?
    ) {
        this.sharingKey = sharingKey
        this.createdCallback = createdCallback
        this.statusCallback = statusCallback
        this.keyNotFoundCallback = keyNotFoundCallback
        this.mySign = Utils.getRandomId()

        getInfoDBReference(sharingKey).addValueEventListener(this)
    }

    override fun unregisterTimerUpdate() {
        this.createdCallback = null
        this.statusCallback = null
        this.keyNotFoundCallback = null

        sharingKey?.let {
            getInfoDBReference(it).removeEventListener(this)
            getSyncDBReference(it).removeEventListener(this)
            //Deleting this info in firebase db is handled by cron job by firebase functions.
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
                PomodoroStatusWithKey(
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
            val timerPreference = datasnapshot.getValue(SyncableTimerPreference::class.java)
            if (timerPreference != null) {
                sharingKey?.let {
                    getInfoDBReference(it).removeEventListener(this)
                    getSyncDBReference(it).addValueEventListener(this)
                    createdCallback?.invoke(timerPreference)
                }
            } else {
                keyNotFoundCallback?.invoke()
            }
        } else if (datasnapshot.key == SYNC_REF_KEY) {
            datasnapshot.getValue(PomodoroStatusWithKey::class.java)?.let { pomodoroStatusWithKey ->
                if (pomodoroStatusWithKey.sign != mySign) {
                    statusCallback?.invoke(pomodoroStatusWithKey.reCorrectedPomodoroStatus(System.currentTimeMillis()))
                }
            }
        }
    }

    private fun getInfoDBReference(sharingKey: String) =
        Firebase.database.getReference(ROOT_REF_KEY).child(sharingKey).child(INFO_REF_KEY)

    private fun getSyncDBReference(sharingKey: String) =
        Firebase.database.getReference(ROOT_REF_KEY).child(sharingKey).child(SYNC_REF_KEY)

    companion object {

        private const val ROOT_REF_KEY = "timer"
        const val INFO_REF_KEY = "info"
        const val SYNC_REF_KEY = "sync"
    }
}