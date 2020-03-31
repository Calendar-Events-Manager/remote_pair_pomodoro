package com.mymeetings.pairpomodoro

import android.app.*
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.IBinder
import android.os.PowerManager
import android.os.PowerManager.PARTIAL_WAKE_LOCK
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat
import androidx.lifecycle.Observer
import com.mymeetings.pairpomodoro.model.PomoState
import com.mymeetings.pairpomodoro.model.PomodoroMaintainer
import com.mymeetings.pairpomodoro.model.PomodoroStatus
import com.mymeetings.pairpomodoro.model.pomodoroAlarm.AndroidTimerAlarm
import com.mymeetings.pairpomodoro.model.pomodoroAlarm.TimerAlarm
import com.mymeetings.pairpomodoro.model.pomodoroPreference.TimerPreference
import com.mymeetings.pairpomodoro.model.pomodoroPreference.UserTimerPreference
import com.mymeetings.pairpomodoro.view.MainActivity
import java.util.*

class PomodoroService : Service(), Observer<PomodoroStatus> {

    private var previousPomoState: PomoState? = null
    private var previousPauseState: Boolean? = null

    private lateinit var timerAlarm: TimerAlarm
    private lateinit var timerPreference: TimerPreference
    private lateinit var wakeLock: PowerManager.WakeLock

    override fun onCreate() {
        super.onCreate()

        timerAlarm = AndroidTimerAlarm(this)
        timerPreference = UserTimerPreference(this)
        wakeLock = (getSystemService(Context.POWER_SERVICE) as PowerManager).newWakeLock(
            PARTIAL_WAKE_LOCK,
            packageName
        )

        PomodoroMaintainer.getPomodoroStatusLiveData().observeForever(this)
    }

    override fun onBind(intent: Intent?): IBinder? {
        return null
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {

        val timerType = intent?.getIntExtra(TIMER_TYPE_KEY, TIMER_TYPE_CREATE) ?: TIMER_TYPE_CREATE

        if (timerType == TIMER_TYPE_CLOSE) {

            PomodoroMaintainer.clear()

            stopForeground(true)
            stopSelf()

            return START_NOT_STICKY
        } else {
            val sharingKey = intent?.getStringExtra(SHARING_KEY)

            when (timerType) {
                TIMER_TYPE_CREATE -> {
                    PomodoroMaintainer.createOwnPomodoro(
                        timerAlarm,
                        timerPreference
                    )
                    PomodoroMaintainer.start()
                }
                TIMER_TYPE_SYNC -> {
                    if (sharingKey != null) {
                        PomodoroMaintainer.syncPomodoro(
                            sharingKey.toUpperCase(Locale.getDefault()).trim(),
                            timerAlarm
                        )
                    }
                }
            }

            startForeground(NOTIF_ID, getMyActivityNotification("Starting pomodoro"))

            return START_STICKY
        }
    }

    override fun onDestroy() {
        super.onDestroy()

        PomodoroMaintainer.getPomodoroStatusLiveData().removeObserver(this)
    }

    override fun onChanged(pomodoroStatus: PomodoroStatus?) {
        checkAndUpdateNotifiation(pomodoroStatus)
        checkAndUpdateCPUWake(pomodoroStatus)
    }

    private fun checkAndUpdateCPUWake(pomodoroStatus: PomodoroStatus?) {
        if (previousPauseState != pomodoroStatus?.pause) {
            if (pomodoroStatus == null || pomodoroStatus.pause) {
                if (wakeLock.isHeld) {
                    wakeLock.release()
                }
            } else {
                wakeLock.acquire(pomodoroStatus.balanceTime)
            }
        }
    }

    private fun checkAndUpdateNotifiation(pomodoroStatus: PomodoroStatus?) {
        if (pomodoroStatus != null && pomodoroStatus.pomoState != previousPomoState) {
            previousPomoState = pomodoroStatus.pomoState

            val notification = getMyActivityNotification(pomodoroStatus.pomoState.name)

            val mNotificationManager =
                getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            mNotificationManager.notify(NOTIF_ID, notification)
        }
    }

    private fun getMyActivityNotification(titleText: String): Notification { // The PendingIntent to launch our activity if the user selects
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val serviceChannel = NotificationChannel(
                "pomodoro",
                "Pomodoro",
                NotificationManager.IMPORTANCE_DEFAULT
            )
            getSystemService(NotificationManager::class.java).also { notificationManager ->
                notificationManager?.createNotificationChannel(serviceChannel)
            }
        }
        val notificationIntent = Intent(this, MainActivity::class.java)
        val pendingIntent = PendingIntent.getActivity(
            this,
            0, notificationIntent, 0
        )
        return NotificationCompat.Builder(this, "pomodoro")
            .setContentTitle(titleText)
            .setContentText("Pomodoro is running in foreground")
            .setWhen(System.currentTimeMillis())
            .setUsesChronometer(true)
            .setSmallIcon(R.drawable.ic_stat_pomodoro)
            .setContentIntent(pendingIntent)
            .build()
    }

    companion object {
        private const val NOTIF_ID = 1
        private const val TIMER_TYPE_KEY = "timer_type"
        private const val SHARING_KEY = "sharing_key"
        private const val TIMER_TYPE_CREATE = 1
        private const val TIMER_TYPE_SYNC = 2
        private const val TIMER_TYPE_CLOSE = 3

        fun createOwnPomodoro(context: Context) {
            val intent = Intent(context, PomodoroService::class.java)
            intent.putExtra(TIMER_TYPE_KEY, TIMER_TYPE_CREATE)
            ContextCompat.startForegroundService(context, intent)
        }

        fun syncPomodoro(context: Context, sharingKey: String) {
            val intent = Intent(context, PomodoroService::class.java)
            intent.putExtra(TIMER_TYPE_KEY, TIMER_TYPE_SYNC)
            intent.putExtra(SHARING_KEY, sharingKey)
            ContextCompat.startForegroundService(context, intent)
        }

        fun stopPomodoro(context: Context) {
            val intent = Intent(context, PomodoroService::class.java)
            intent.putExtra(TIMER_TYPE_KEY, TIMER_TYPE_CLOSE)
            ContextCompat.startForegroundService(context, intent)
        }
    }
}