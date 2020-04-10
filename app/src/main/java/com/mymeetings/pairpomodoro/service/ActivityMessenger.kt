package com.mymeetings.pairpomodoro.service

import android.os.*
import com.mymeetings.pairpomodoro.model.PomodoroStatus
import com.mymeetings.pairpomodoro.model.pomodoroPreference.TimerPreference
import com.mymeetings.pairpomodoro.model.pomodoroPreference.UserTimerPreferenceBuilder
import com.mymeetings.pairpomodoro.service.MessengerProtocol.Command

class ActivityMessenger(
    onTimerCreated: (TimerPreference, sharingKey: String) -> Unit,
    onStatusUpdated: (pomodoroStatus: PomodoroStatus?) -> Unit,
    onKeyNotFound: () -> Unit
) {

    private var sendingMessenger: Messenger? = null
    private val receivingMessenger = Messenger(
        ReplyHandler(
            onTimerCreated,
            onStatusUpdated,
            onKeyNotFound
        )
    )

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

    fun handShake() {
        sendMessage(MessengerProtocol.COMMAND_HANDSHAKE)
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
        msg.replyTo = receivingMessenger

        try {
            sendingMessenger?.send(msg)
        } catch (ignore: RemoteException) {
            //Ignore
        }
    }

    internal class ReplyHandler(
        private val onTimerCreated: (TimerPreference, sharingKey: String) -> Unit,
        private val onStatusUpdated: (pomodoroStatus: PomodoroStatus?) -> Unit,
        private val onKeyNotFound: () -> Unit
    ) : Handler() {
        override fun handleMessage(msg: Message) {
            super.handleMessage(msg)

            when (msg.data?.getInt(MessengerProtocol.REPLY_KEY)) {
                MessengerProtocol.REPLY_STATUS -> {
                    val pomodoroStatus =
                        msg.data?.getParcelable<PomodoroStatus>(MessengerProtocol.STATUS_KEY)
                    onStatusUpdated.invoke(pomodoroStatus)
                }
                MessengerProtocol.REPLY_NOT_FOUND -> {
                    onKeyNotFound.invoke()
                }
                MessengerProtocol.REPLY_CREATED -> {
                    val sharingKey = msg.data?.getString(MessengerProtocol.SYNC_KEY) ?: ""
                    val preference = msg.data?.getParcelable(MessengerProtocol.PREFERENCE_KEY)
                        ?: UserTimerPreferenceBuilder.buildDefault()
                    onTimerCreated(preference, sharingKey)
                }
            }
        }
    }
}