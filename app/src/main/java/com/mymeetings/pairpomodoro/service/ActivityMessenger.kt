package com.mymeetings.pairpomodoro.service

import android.os.*
import com.mymeetings.pairpomodoro.model.PomodoroStatus
import com.mymeetings.pairpomodoro.service.MessengerProtocol.Command

class ActivityMessenger(
    statusCallback: (pomodoroStatus: PomodoroStatus?, sharingKey: String?) -> Unit,
    keyNotFoundCallback: (() -> Unit)
) {

    private var sendingMessenger: Messenger? = null
    private val recievingMessenger = Messenger(ReplyHandler(statusCallback, keyNotFoundCallback))

    fun onConnect(messenger: Messenger) {
        this.sendingMessenger = messenger
    }

    fun onDisconnect() {
        this.sendingMessenger = null
    }

    fun createOwnPomodoro() {
        sendMessage(MessengerProtocol.COMMAND_CREATE)
    }

    fun syncPomodoro(sharingKey: String) {
        sendMessage(MessengerProtocol.COMMAND_SYNC, sharingKey)
    }

    fun startPomodoro() {
        sendMessage(MessengerProtocol.COMMAND_START)
    }

    fun pausePomodoro() {
        sendMessage(MessengerProtocol.COMMAND_PAUSE)
    }

    fun resetPomodoro() {
        sendMessage(MessengerProtocol.COMMAND_RESET)
    }

    fun closePomodoro() {
        sendMessage(MessengerProtocol.COMMAND_CLOSE)
    }

    /**
     * sharingKey is optional and should be only sent for syncPomodoro.
     */
    private fun sendMessage(@Command command: Int, sharingKey: String? = null) {
        val msg = Message.obtain()

        val bundle = Bundle()
        bundle.putInt(MessengerProtocol.COMMAND_TYPE_KEY, command)
        bundle.putString(MessengerProtocol.SYNC_KEY, sharingKey)

        msg.data = bundle
        msg.replyTo = recievingMessenger

        try {
            sendingMessenger?.send(msg)
        } catch (ignore: RemoteException) {
            //Ignore
        }
    }

    internal class ReplyHandler(
        private val statusCallback: (pomodoroStatus: PomodoroStatus?, sharingKey: String?) -> Unit,
        private val keyNotFoundCallback: (() -> Unit)
    ) : Handler() {
        override fun handleMessage(msg: Message) {
            super.handleMessage(msg)

            val reply = msg.data?.getInt(MessengerProtocol.REPLY_KEY)

            when (reply) {
                MessengerProtocol.REPLY_STATUS -> {
                    val pomodoroStatus =
                        msg.data?.getParcelable<PomodoroStatus>(MessengerProtocol.STATUS_KEY)
                    val sharingKey = msg.data?.getString(MessengerProtocol.SYNC_KEY)
                    statusCallback.invoke(pomodoroStatus, sharingKey)
                }
                MessengerProtocol.REPLY_NOT_FOUND -> {
                    keyNotFoundCallback.invoke()
                }
            }
        }
    }
}