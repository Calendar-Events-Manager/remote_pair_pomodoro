package com.mymeetings.pairpomodoro

import android.app.*
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.IBinder
import androidx.core.app.NotificationCompat
import androidx.lifecycle.Observer
import com.mymeetings.pairpomodoro.model.PomoState
import com.mymeetings.pairpomodoro.model.PomodoroMaintainer
import com.mymeetings.pairpomodoro.model.PomodoroStatus
import com.mymeetings.pairpomodoro.view.MainActivity


class PomodorService : Service(), Observer<PomodoroStatus> {

    private var pomoState: PomoState? = null

    override fun onCreate() {
        super.onCreate()

        PomodoroMaintainer.getPomodoroStatusLiveData().observeForever(this)
    }

    override fun onBind(intent: Intent?): IBinder? {
        return null
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {

        val notification = getMyActivityNotification("Pomodoro started")
        startForeground(NOTIF_ID, notification)

        return START_STICKY
    }

    override fun onDestroy() {
        super.onDestroy()

        PomodoroMaintainer.getPomodoroStatusLiveData().removeObserver(this)
    }

    override fun onChanged(pomodoroStatus: PomodoroStatus?) {
        if (pomodoroStatus != null && pomodoroStatus.pomoState != pomoState) {
            pomoState = pomodoroStatus.pomoState

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
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setContentIntent(pendingIntent)
            .build()
    }

    companion object {
        private const val NOTIF_ID = 1
    }
}