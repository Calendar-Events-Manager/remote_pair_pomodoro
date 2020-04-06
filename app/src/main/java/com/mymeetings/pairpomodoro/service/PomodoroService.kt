package com.mymeetings.pairpomodoro.service

import android.app.Service
import android.content.Context
import android.content.Intent
import android.os.IBinder
import android.os.PowerManager
import android.os.PowerManager.PARTIAL_WAKE_LOCK
import com.mymeetings.pairpomodoro.model.PomodoroStatus
import com.mymeetings.pairpomodoro.model.pomodoroAlarm.AndroidTimerAlarm
import com.mymeetings.pairpomodoro.model.pomodoroManager.PomodoroManager
import com.mymeetings.pairpomodoro.model.pomodoroPreference.UserTimerPreference
import com.mymeetings.pairpomodoro.model.pomodoroSyncer.FirebaseTimerSyncer
import com.mymeetings.pairpomodoro.view.NotificationUtils

class PomodoroService : Service() {

    private var pomodoroStatus: PomodoroStatus? = null
    private var isForeGround: Boolean = false
    private val serviceMessenger = ServiceMessenger(::onCommandReceived)

    private val pomodoroManager by lazy {
        PomodoroManager(
            AndroidTimerAlarm(this),
            FirebaseTimerSyncer(),
            ::onPomodoroStatusUpdate
        )
    }

    private val wakeLock by lazy {
        (getSystemService(Context.POWER_SERVICE) as PowerManager).newWakeLock(
            PARTIAL_WAKE_LOCK,
            packageName
        )
    }

    private fun startForeGround() {
        isForeGround = true
        startForeground(
            NotificationUtils.POMODORO_NOTIFICATION_ID,
            NotificationUtils.getPomodoroNotification(this, pomodoroStatus)
        )
    }

    private fun stopForeground() {
        isForeGround = false
        stopForeground(true)
    }

    override fun onBind(intent: Intent?): IBinder? {
        stopForeground(true)
        return serviceMessenger.getBinder()
    }

    override fun onRebind(intent: Intent?) {
        super.onRebind(intent)
        stopForeground()
    }

    override fun onUnbind(intent: Intent?): Boolean {
        if (pomodoroManager.isRunning()) {
            startForeGround()
        } else {
            stopForeground()
        }

        return true
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {

        val timerType = intent?.getIntExtra(
            TIMER_TYPE_KEY,
            -1
        ) ?: -1

        when (timerType) {
            TIMER_TYPE_START -> {
                pomodoroManager.start()
            }
            TIMER_TYPE_PAUSE -> {
                pomodoroManager.pause()
            }
            TIMER_TYPE_EXIT -> {
                closePomodoroTimer()
            }
        }

        return START_NOT_STICKY
    }

    private fun onPomodoroStatusUpdate(pomodoroStatus: PomodoroStatus) {
        if (this.pomodoroStatus?.pause != pomodoroStatus.pause) {
            checkAndUpdateNotification(pomodoroStatus)
            checkAndUpdateCPUWake(pomodoroStatus)
        }
        this.pomodoroStatus = pomodoroStatus
        serviceMessenger.sendPomodoroStatus(pomodoroManager.getShareKey(), pomodoroStatus)
    }

    private fun onSharingFailed() {
        serviceMessenger.sendKeyNotFound()
    }

    private fun onCommandReceived(@MessengerProtocol.Command command: Int, sharingKey: String? = null) {
        when (command) {
            MessengerProtocol.COMMAND_HANDSHAKE -> {
                serviceMessenger.sendPomodoroStatus(pomodoroManager.getShareKey(), pomodoroStatus)
            }
            MessengerProtocol.COMMAND_CREATE -> {
                pomodoroManager.create(UserTimerPreference(this))
                pomodoroManager.start()
            }
            MessengerProtocol.COMMAND_SYNC -> {
                if (sharingKey != null) {
                    pomodoroManager.sync(
                        sharingKey,
                        ::onSharingFailed
                    )
                }
            }
            MessengerProtocol.COMMAND_START -> {
                pomodoroManager.start()
            }
            MessengerProtocol.COMMAND_PAUSE -> {
                pomodoroManager.pause()
            }
            MessengerProtocol.COMMAND_RESET -> {
                pomodoroManager.reset()
            }
            MessengerProtocol.COMMAND_CLOSE -> {
                closePomodoroTimer()
            }
        }
    }

    private fun closePomodoroTimer() {
        pomodoroStatus = null
        pomodoroManager.close()
        serviceMessenger.sendPomodoroStatus()
        stopForeground()
    }

    override fun onDestroy() {
        super.onDestroy()

        if (wakeLock.isHeld) {
            wakeLock.release()
        }
    }

    private fun checkAndUpdateCPUWake(pomodoroStatus: PomodoroStatus?) {
        if (pomodoroStatus == null || pomodoroStatus.pause) {
            if (wakeLock.isHeld) {
                wakeLock.release()
            }
        } else {
            wakeLock.acquire(pomodoroStatus.balanceTime)
        }
    }


    private fun checkAndUpdateNotification(pomodoroStatus: PomodoroStatus?) {
        if (isForeGround) {
            NotificationUtils.showRunningNotification(this, pomodoroStatus)
        }
    }

    companion object {
        private const val TIMER_TYPE_KEY = "timer_type"
        private const val TIMER_TYPE_PAUSE = 4
        private const val TIMER_TYPE_START = 5
        private const val TIMER_TYPE_EXIT = 6

        fun getStartPomodoroIntent(context: Context) =
            Intent(context, PomodoroService::class.java).apply {
                putExtra(
                    TIMER_TYPE_KEY,
                    TIMER_TYPE_START
                )
            }

        fun getPausePomodoroIntent(context: Context) =
            Intent(context, PomodoroService::class.java).apply {
                putExtra(
                    TIMER_TYPE_KEY,
                    TIMER_TYPE_PAUSE
                )
            }

        fun getExitPomodoroIntent(context: Context) =
            Intent(context, PomodoroService::class.java).apply {
                putExtra(
                    TIMER_TYPE_KEY,
                    TIMER_TYPE_EXIT
                )
            }

    }

}