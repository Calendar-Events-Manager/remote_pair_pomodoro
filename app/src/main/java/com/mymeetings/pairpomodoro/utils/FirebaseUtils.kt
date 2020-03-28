package com.mymeetings.pairpomodoro.utils

import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import com.mymeetings.pairpomodoro.model.pomodoroManager.PomoStatusWithKey
import com.mymeetings.pairpomodoro.model.timerPreference.SyncableTimerPreference


object FirebaseUtils {

    private const val ROOT_REF_KEY = "timer"
    const val INFO_REF_KEY = "info"
    const val SYNC_REF_KEY = "sync"

    private fun getInfoDBReference(sharingKey: String) =
        Firebase.database.getReference(ROOT_REF_KEY).child(sharingKey).child(
            INFO_REF_KEY
        )

    private fun getSyncDBReference(sharingKey: String) =
        Firebase.database.getReference(ROOT_REF_KEY).child(sharingKey).child(
            SYNC_REF_KEY
        )

    private fun getMasterDbReference(sharingKey: String) =
        Firebase.database.getReference(ROOT_REF_KEY).child(sharingKey)


    fun listenToStatusSyncData(sharingKey: String, valueEventListener: ValueEventListener) {
        getSyncDBReference(sharingKey).addValueEventListener(valueEventListener)
    }

    fun listenToInfoData(sharingKey: String, valueEventListener: ValueEventListener) {
        getInfoDBReference(sharingKey).addValueEventListener(valueEventListener)
    }

    fun clearData(sharingKey: String, valueEventListener: ValueEventListener) {
        getInfoDBReference(sharingKey).removeEventListener(valueEventListener)
        getSyncDBReference(sharingKey).removeEventListener(valueEventListener)
        getMasterDbReference(sharingKey).removeValue()
    }

    fun setTimerInfoData(sharingKey: String, timerPreference: SyncableTimerPreference) {
        getInfoDBReference(sharingKey).setValue(timerPreference)
    }

    fun syncTimerData(sharingKey: String, pomoStatusWithKey: PomoStatusWithKey) {
        getSyncDBReference(sharingKey).setValue(pomoStatusWithKey)
    }

    fun clearDataOnDisconnect(shareKey: String) {
        getMasterDbReference(shareKey).onDisconnect().removeValue()
    }
}