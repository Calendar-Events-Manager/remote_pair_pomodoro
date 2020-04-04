package com.mymeetings.pairpomodoro.service

import android.app.Service
import android.content.Context
import android.content.Intent
import android.os.*
import android.os.PowerManager.PARTIAL_WAKE_LOCK
import androidx.core.content.ContextCompat
import androidx.lifecycle.Observer
import com.mymeetings.pairpomodoro.model.PomodoroMaintainer
import com.mymeetings.pairpomodoro.model.PomodoroStatus
import com.mymeetings.pairpomodoro.model.pomodoroAlarm.AndroidTimerAlarm
import com.mymeetings.pairpomodoro.model.pomodoroAlarm.TimerAlarm
import com.mymeetings.pairpomodoro.model.pomodoroPreference.TimerPreference
import com.mymeetings.pairpomodoro.model.pomodoroPreference.UserTimerPreference
import com.mymeetings.pairpomodoro.view.NotificationUtils
import java.util.*

class PomodoroService : Service() {

    private var pomodoroStatus: PomodoroStatus? = null

    private lateinit var timerAlarm: TimerAlarm
    private lateinit var timerPreference: TimerPreference
    private lateinit var wakeLock: PowerManager.WakeLock
    private val pomodoroMaintainer = PomodoroMaintainer()
    private val serviceMessenger = ServiceMessenger(::onCommandRecieved)

    private val statusObserver = Observer<PomodoroStatus> { pomodoroStatus ->
        serviceMessenger.sendPomodoroStatus(pomodoroMaintainer.shareKey(), pomodoroStatus)
        if (this.pomodoroStatus?.pause != pomodoroStatus?.pause) {
            if (pomodoroStatus != null) {
                checkAndUpdateNotification(pomodoroStatus)
            }
            checkAndUpdateCPUWake(pomodoroStatus)
            this.pomodoroStatus = pomodoroStatus
        }
    }

    private val syncFailedObserver = Observer<Unit> {
        serviceMessenger.sendKeyNotFound()
    }

    override fun onCreate() {
        super.onCreate()

        timerAlarm = AndroidTimerAlarm(this)
        timerPreference = UserTimerPreference(this)
        wakeLock = (getSystemService(Context.POWER_SERVICE) as PowerManager).newWakeLock(
            PARTIAL_WAKE_LOCK,
            packageName
        )

        pomodoroMaintainer.getPomodoroStatusLiveData().observeForever(statusObserver)
        pomodoroMaintainer.getSyncFailedLiveData().observeForever(syncFailedObserver)
    }

    override fun onBind(intent: Intent?): IBinder? {
        return serviceMessenger.getBinder()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {

        val timerType = intent?.getIntExtra(
            TIMER_TYPE_KEY,
            TIMER_TYPE_CREATE
        ) ?: TIMER_TYPE_CREATE

        if (timerType == TIMER_TYPE_CLOSE) {

            pomodoroMaintainer.clear()

            stopForeground(true)
            stopSelf()

            return START_NOT_STICKY
        } else {

            if (pomodoroStatus == null) {
                startForeground(
                    NotificationUtils.POMODORO_NOTIFICATION_ID,
                    NotificationUtils.getPomodoroNotification(this)
                )
            }

            val sharingKey = intent?.getStringExtra(SHARING_KEY)

            when (timerType) {
                TIMER_TYPE_CREATE -> {
                    pomodoroMaintainer.createOwnPomodoro(
                        timerAlarm,
                        timerPreference
                    )
                    pomodoroMaintainer.start()
                }
                TIMER_TYPE_SYNC -> {
                    if (sharingKey != null) {
                        pomodoroMaintainer.syncPomodoro(
                            sharingKey.toUpperCase(Locale.getDefault()).trim(),
                            timerAlarm
                        )
                    }
                }
                TIMER_TYPE_START -> {
                    pomodoroMaintainer.start()
                }
                TIMER_TYPE_PAUSE -> {
                    pomodoroMaintainer.pause()
                }
                TIMER_TYPE_RESET -> {
                    pomodoroMaintainer.reset()
                }
            }

            return START_STICKY
        }
    }

    fun onCommandRecieved(@MessengerProtocol.Command command: Int, sharingKey: String? = null) {
        when (command) {
            MessengerProtocol.COMMAND_CREATE -> {
                pomodoroMaintainer.createOwnPomodoro(
                    timerAlarm,
                    timerPreference
                )
                pomodoroMaintainer.start()
            }
            MessengerProtocol.COMMAND_SYNC -> {
                if (sharingKey != null) {
                    pomodoroMaintainer.syncPomodoro(
                        sharingKey.toUpperCase(Locale.getDefault()).trim(),
                        timerAlarm
                    )
                }
            }
            MessengerProtocol.COMMAND_START -> {
                pomodoroMaintainer.start()
            }
            MessengerProtocol.COMMAND_PAUSE -> {
                pomodoroMaintainer.pause()
            }
            MessengerProtocol.COMMAND_RESET -> {
                pomodoroMaintainer.reset()
            }
            MessengerProtocol.COMMAND_CLOSE -> {
                pomodoroMaintainer.clear()
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()

        pomodoroMaintainer.getPomodoroStatusLiveData().removeObserver(statusObserver)
        pomodoroMaintainer.getSyncFailedLiveData().removeObserver(syncFailedObserver)
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
        NotificationUtils.showRunningNotification(this, pomodoroStatus)
    }

    companion object {
        private const val TIMER_TYPE_KEY = "timer_type"
        private const val SHARING_KEY = "sharing_key"
        private const val TIMER_TYPE_CREATE = 1
        private const val TIMER_TYPE_SYNC = 2
        private const val TIMER_TYPE_CLOSE = 3
        private const val TIMER_TYPE_PAUSE = 4
        private const val TIMER_TYPE_START = 5
        private const val TIMER_TYPE_RESET = 6

        fun createOwnPomodoro(context: Context) {
            val intent = Intent(context, PomodoroService::class.java)
            intent.putExtra(
                TIMER_TYPE_KEY,
                TIMER_TYPE_CREATE
            )
            ContextCompat.startForegroundService(context, intent)
        }

        fun syncPomodoro(context: Context, sharingKey: String) {
            val intent = Intent(context, PomodoroService::class.java)
            intent.putExtra(
                TIMER_TYPE_KEY,
                TIMER_TYPE_SYNC
            )
            intent.putExtra(SHARING_KEY, sharingKey)
            ContextCompat.startForegroundService(context, intent)
        }

        fun stopPomodoro(context: Context) {
            val intent = Intent(context, PomodoroService::class.java)
            intent.putExtra(
                TIMER_TYPE_KEY,
                TIMER_TYPE_CLOSE
            )
            ContextCompat.startForegroundService(context, intent)
        }

        fun getStartPomodoroIntent(context: Context) =
            Intent(context, PomodoroService::class.java).apply {
                putExtra(
                    TIMER_TYPE_KEY,
                    TIMER_TYPE_START
                )
            }

        fun startPomodoro(context: Context) =
            ContextCompat.startForegroundService(
                context,
                getStartPomodoroIntent(
                    context
                )
            )


        fun getPausePomodoroIntent(context: Context) =
            Intent(context, PomodoroService::class.java).apply {
                putExtra(
                    TIMER_TYPE_KEY,
                    TIMER_TYPE_PAUSE
                )
            }

        fun pausePomodoro(context: Context) =
            ContextCompat.startForegroundService(
                context,
                getPausePomodoroIntent(
                    context
                )
            )

        fun resetPomodoro(context: Context) {
            val intent = Intent(context, PomodoroService::class.java)
            intent.putExtra(
                TIMER_TYPE_KEY,
                TIMER_TYPE_RESET
            )
            ContextCompat.startForegroundService(context, intent)
        }
    }

}