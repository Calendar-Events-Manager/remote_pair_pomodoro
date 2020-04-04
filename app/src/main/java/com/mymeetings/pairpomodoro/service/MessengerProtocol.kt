package com.mymeetings.pairpomodoro.service

import androidx.annotation.IntDef

object MessengerProtocol {

    const val STATUS_KEY = "status"
    const val COMMAND_TYPE_KEY = "command"
    const val SYNC_KEY = "sync_key"
    const val REPLY_KEY = "reply"

    @Command const val COMMAND_HANDSHAKE = 0
    @Command const val COMMAND_CREATE = 1
    @Command const val COMMAND_SYNC = 2
    @Command const val COMMAND_START = 3
    @Command const val COMMAND_PAUSE = 4
    @Command const val COMMAND_RESET = 5
    @Command const val COMMAND_CLOSE = 6

    @Target(AnnotationTarget.TYPE, AnnotationTarget.VALUE_PARAMETER, AnnotationTarget.PROPERTY)
    @IntDef(COMMAND_HANDSHAKE, COMMAND_CREATE, COMMAND_SYNC, COMMAND_START, COMMAND_PAUSE, COMMAND_RESET, COMMAND_CLOSE)
    @Retention(AnnotationRetention.SOURCE)
    annotation class Command


    @Reply const val REPLY_STATUS = 1
    @Reply const val REPLY_NOT_FOUND = 2

    @IntDef(REPLY_STATUS, REPLY_NOT_FOUND)
    @Retention(AnnotationRetention.SOURCE)
    annotation class Reply

}