package com.mymeetings.pairpomodoro.view.messenger

import android.os.*


class PomoMessenger {

    private var messenger: Messenger? = null
    private val replyMessenger = Messenger(ReplyHandler())

    fun onConnect(messenger: Messenger) {
        this.messenger = messenger
    }

    fun onDisconnect() {
        this.messenger = null
    }

    fun sendCreateOwnPomodoroCommand() {
        val msg = Message.obtain()

        val bundle = Bundle()

        msg.data = bundle
        msg.replyTo = replyMessenger

        try {
            messenger?.send(msg)
        } catch (ignore: RemoteException) {
            //Ignore
        }
    }

    internal class ReplyHandler : Handler() {
        override fun handleMessage(msg: Message) {
            super.handleMessage(msg)

            val bundle = msg.data

            //TODO
        }
    }
}