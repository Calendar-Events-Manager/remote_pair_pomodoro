package com.mymeetings.pairpomodoro.utils

import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase


object FirebaseUtils {

    private const val ROOT_REF_KEY = "timer"
    const val INFO_REF_KEY = "info"
    const val SYNC_REF_KEY = "sync"

    fun getInfoDBReference(sharingKey: String) =
        Firebase.database.getReference(ROOT_REF_KEY).child(sharingKey).child(
            INFO_REF_KEY
        )

    fun getSyncDBReference(sharingKey: String) =
        Firebase.database.getReference(ROOT_REF_KEY).child(sharingKey).child(
            SYNC_REF_KEY
        )

    fun getMasterDbReference(sharingKey: String) = Firebase.database.getReference(ROOT_REF_KEY).child(sharingKey)
}