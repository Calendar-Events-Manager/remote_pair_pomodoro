package com.mymeetings.pairpomodoro.view

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.os.Bundle
import android.os.IBinder
import android.os.Messenger
import androidx.appcompat.app.AppCompatActivity
import androidx.preference.PreferenceManager
import com.mymeetings.pairpomodoro.R
import com.mymeetings.pairpomodoro.model.PomoState
import com.mymeetings.pairpomodoro.model.PomodoroStatus
import com.mymeetings.pairpomodoro.model.pomodoroAlarm.AndroidTimerAlarm
import com.mymeetings.pairpomodoro.model.pomodoroAlarm.TimerAlarmType
import com.mymeetings.pairpomodoro.service.ActivityMessenger
import com.mymeetings.pairpomodoro.service.PomodoroService
import com.mymeetings.pairpomodoro.utils.Utils
import com.mymeetings.pairpomodoro.view.preference.PreferenceActivity
import kotlinx.android.synthetic.main.activity_main.*


class MainActivity : AppCompatActivity(), ServiceConnection {

    private val activityMessenger = ActivityMessenger(::showStatus, ::keyNotFound)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        pauseButton.setOnClickListener {
            activityMessenger.pausePomodoro()
        }

        startButton.setOnClickListener {
            activityMessenger.startPomodoro()
        }

        resetButton.setOnClickListener {
            ViewUtils.confirmationDialog(this, getString(R.string.reset_timer)) {
                if (it) {
                    activityMessenger.resetPomodoro()
                }
            }
        }

        createButton.setOnClickListener {
            activityMessenger.createOwnPomodoro()
        }

        syncButton.setOnClickListener {
            ViewUtils.buildInputDialog(this) {
                activityMessenger.syncPomodoro(it)
                showTimerView()
            }
        }

        closeButton.setOnClickListener {
            ViewUtils.confirmationDialog(this, getString(R.string.exit_app)) {
                if (it) {
                    activityMessenger.closePomodoro()
                }
            }
        }

        sharingKeyText.setOnClickListener {
            ViewUtils.copyTextToClipBoard(this, sharingKeyText.text.toString())
        }

        settingsButton.setOnClickListener {
            PreferenceActivity.open(this)
        }

        showSelectionView()

        startService(Intent(this, PomodoroService::class.java))
    }

    private fun showStatus(pomodoroStatus: PomodoroStatus?, sharingKey: String?) {
        if (pomodoroStatus != null) {
            showTimerView(pomodoroStatus, sharingKey)
        } else {
            showSelectionView()
        }
    }

    private fun keyNotFound() {
        ViewUtils.infoDialog(this, "Timer not available for this key") {
            activityMessenger.closePomodoro()
        }
    }

    private fun showTimerView(
        pomodoroStatus: PomodoroStatus? = null,
        sharingKey: String? = null
    ) {

        timerLayout.visible()
        selectionLayout.gone()

        countDownView.text = Utils.getDurationBreakdown(pomodoroStatus?.balanceTime ?: 0)

        when {
            pomodoroStatus == null -> {
                startButton.gone()
                pauseResetLayout.gone()
                progressBar.visible()
                pausedText.gone()
            }
            pomodoroStatus.pause -> {
                startButton.visible()
                pauseResetLayout.gone()
                progressBar.gone()
                pausedText.visible()
            }
            else -> {
                startButton.gone()
                pauseResetLayout.visible()
                progressBar.gone()
                pausedText.gone()
            }
        }

        sharingKeyText.text = sharingKey ?: ""

        val mode = when (pomodoroStatus?.pomoState ?: PomoState.Focus) {
            PomoState.Focus -> {
                "Focus"
            }
            PomoState.LongBreak -> {
                "Long Break"
            }
            PomoState.ShortBreak -> {
                "Short Break"
            }
        }
        modeText.text = mode

        val startText = when (pomodoroStatus?.pomoState ?: PomoState.Focus) {
            PomoState.Focus -> {
                "Start Focus"
            }
            PomoState.LongBreak -> {
                "Take Long Break"
            }
            PomoState.ShortBreak -> {
                "Take Short Break"
            }
        }
        startButton.text = startText


        container.keepScreenOn =
            PreferenceManager.getDefaultSharedPreferences(this).getBoolean("screen_on", true)
    }

    override fun onStart() {
        super.onStart()
        bindService(Intent(this, PomodoroService::class.java), this, Context.BIND_AUTO_CREATE)
    }

    override fun onStop() {
        super.onStop()
        unbindService(this)
    }

    private fun showSelectionView() {
        selectionLayout.visible()
        timerLayout.gone()
        container.keepScreenOn = false
    }

    override fun onServiceDisconnected(name: ComponentName?) {
        activityMessenger.onDisconnect()
    }

    override fun onServiceConnected(name: ComponentName?, service: IBinder?) {
        activityMessenger.onConnect(Messenger(service))
        activityMessenger.handShake()
    }
}
