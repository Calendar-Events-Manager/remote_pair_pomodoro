package com.mymeetings.pairpomodoro.service

import android.os.*
import com.mymeetings.pairpomodoro.model.PomodoroStatus
import com.mymeetings.pairpomodoro.model.pomodoroPreference.TimerPreference
import com.mymeetings.pairpomodoro.service.MessengerProtocol.Command
import com.mymeetings.pairpomodoro.service.MessengerProtocol.Reply

class ServiceMessenger(
    commandCallback: ((@Command Int, sharingKey: String?) -> Unit)
) {

    private var sendingMessenger: Messenger? = null
    private val receivingMessenger = Messenger(ReplyHandler(commandCallback, { messenger ->
        sendingMessenger = messenger
    }))

    fun getBinder(): IBinder = receivingMessenger.binder

    fun sendPomodoroStatus(
        pomodoroStatus: PomodoroStatus? = null) {
        sendMessage(
            reply = MessengerProtocol.REPLY_STATUS,
            pomodoroStatus = pomodoroStatus
        )
    }

    fun sendTimerCreated(timerPreference: TimerPreference, sharingKey: String) {
        sendMessage(
            reply = MessengerProtocol.REPLY_CREATED,
            sharingKey = sharingKey,
            timerPreference = timerPreference
        )
    }

    fun sendKeyNotFound() {
        sendMessage(MessengerProtocol.REPLY_NOT_FOUND)
    }

    /**
     * sharingKey is optional and should be only sent for pomodoro status.
     */
    private fun sendMessage(
        @Reply reply: Int,
        sharingKey: String? = null,
        pomodoroStatus: PomodoroStatus? = null,
        timerPreference: TimerPreference? = null
    ) {
        val msg = Message.obtain()

        val bundle = Bundle()
        bundle.putInt(MessengerProtocol.REPLY_KEY, reply)
        bundle.putString(MessengerProtocol.SYNC_KEY, sharingKey)
        bundle.putParcelable(MessengerProtocol.STATUS_KEY, pomodoroStatus)
        bundle.putParcelable(MessengerProtocol.PREFERENCE_KEY, timerPreference)

        msg.data = bundle
        msg.replyTo = receivingMessenger

        try {
            sendingMessenger?.send(msg)
        } catch (ignore: RemoteException) {
            //Ignore
        }
    }

    internal class ReplyHandler(
        private val commandCallback: ((@Command Int, sharingKey: String?) -> Unit),
        private val replyReceiverCallback: ((replyMessenger: Messenger?) -> Unit)
    ) : Handler() {
        override fun handleMessage(msg: Message) {
            super.handleMessage(msg)

            val command = msg.data?.getInt(MessengerProtocol.COMMAND_TYPE_KEY)
            val sharingKey = msg.data?.getString(MessengerProtocol.SYNC_KEY)
            replyReceiverCallback.invoke(msg.replyTo)

            command?.let {
                commandCallback(command, sharingKey)
            }
        }
    }
}