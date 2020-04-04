package com.mymeetings.pairpomodoro.view

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import com.mymeetings.pairpomodoro.service.PomodoroService
import com.mymeetings.pairpomodoro.R
import com.mymeetings.pairpomodoro.model.PomodoroStatus

object NotificationUtils {

    private const val FOREGROUND_CHANNEL_ID = "pomodoro_timer"
    private const val REQ_CODE_OPEN_ACTIVITY = 1
    private const val REQ_CODE_START_TIMER = 2
    private const val REQ_CODE_PAUSE_TIMER = 3
    const val POMODORO_NOTIFICATION_ID = 12

    private fun checkAndCreateChannel(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {

            context.getSystemService(NotificationManager::class.java)?.let { notificationManager ->

                if (notificationManager.getNotificationChannel(FOREGROUND_CHANNEL_ID) == null) {

                    val serviceChannel = NotificationChannel(
                        FOREGROUND_CHANNEL_ID,
                        FOREGROUND_CHANNEL_ID,
                        NotificationManager.IMPORTANCE_DEFAULT
                    )

                    notificationManager.createNotificationChannel(serviceChannel)
                }
            }
        }

    }

    fun getPomodoroNotification(
        context: Context,
        pomodoroStatus: PomodoroStatus? = null
    ): Notification {

        checkAndCreateChannel(context)

        val notificationIntent = Intent(context, MainActivity::class.java)
        val pendingIntent = PendingIntent.getActivity(
            context,
            REQ_CODE_OPEN_ACTIVITY,
            notificationIntent,
            PendingIntent.FLAG_UPDATE_CURRENT
        )
        val notificationBuilder = NotificationCompat.Builder(context, FOREGROUND_CHANNEL_ID)
            .setContentTitle(context.getString(R.string.pomodoro))
            .setWhen(System.currentTimeMillis())
            .setSmallIcon(R.drawable.ic_stat_pomodoro)
            .setContentIntent(pendingIntent)


        when {
            pomodoroStatus == null -> {
                notificationBuilder.setContentText(context.getString(R.string.timer_starting))
            }
            pomodoroStatus.pause -> {

                val actionPendingIntent: PendingIntent = PendingIntent.getService(
                    context,
                    REQ_CODE_START_TIMER,
                    PomodoroService.getStartPomodoroIntent(context),
                    PendingIntent.FLAG_UPDATE_CURRENT
                )

                val startAction = NotificationCompat.Action(
                    R.drawable.ic_play_arrow_white_24dp,
                    context.getString(R.string.start),
                    actionPendingIntent
                )

                notificationBuilder
                    .setUsesChronometer(true)
                    .setContentText(context.getString(R.string.paused))
                    .addAction(startAction)

            }
            else -> {
                val actionPendingIntent: PendingIntent = PendingIntent.getService(
                    context,
                    REQ_CODE_PAUSE_TIMER,
                    PomodoroService.getPausePomodoroIntent(context),
                    PendingIntent.FLAG_UPDATE_CURRENT
                )

                val pauseAction = NotificationCompat.Action(
                    R.drawable.ic_pause_white_24dp,
                    context.getString(R.string.pause),
                    actionPendingIntent
                )

                notificationBuilder.addAction(pauseAction)
                    .setWhen(System.currentTimeMillis() + pomodoroStatus.balanceTime)
                    .setUsesChronometer(true)
                    .setContentText(context.getString(R.string.started))
            }
        }

        return notificationBuilder.build()
    }

    fun showRunningNotification(
        context: Context,
        pomodoroStatus: PomodoroStatus?
    ) {
        (context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager).notify(
            POMODORO_NOTIFICATION_ID,
            getPomodoroNotification(context, pomodoroStatus)
        )

    }
}