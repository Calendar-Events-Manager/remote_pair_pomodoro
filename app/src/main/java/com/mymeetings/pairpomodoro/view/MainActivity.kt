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
import com.mymeetings.pairpomodoro.model.PomodoroState
import com.mymeetings.pairpomodoro.model.PomodoroStatus
import com.mymeetings.pairpomodoro.model.pomodoroPreference.TimerPreference
import com.mymeetings.pairpomodoro.service.ActivityMessenger
import com.mymeetings.pairpomodoro.service.PomodoroService
import com.mymeetings.pairpomodoro.utils.Utils
import com.mymeetings.pairpomodoro.view.preference.PreferenceActivity
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity(), ServiceConnection {

    private var timerPreference: TimerPreference? = null

    private val activityMessenger = ActivityMessenger(
        ::onTimerCreated,
        ::onStatusUpdated,
        ::onKeyNotFound
    )

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

    private fun onStatusUpdated(pomodoroStatus: PomodoroStatus?) {
        if (pomodoroStatus != null) {
            showTimerView(pomodoroStatus)
        } else {
            showSelectionView()
        }
    }

    private fun onTimerCreated(
        timerPreference: TimerPreference,
        sharingKey: String
    ) {
        this.timerPreference = timerPreference
        sharingKeyText.text = sharingKey
    }

    private fun onKeyNotFound() {
        ViewUtils.infoDialog(this, "Timer not available for this key") {
            activityMessenger.closePomodoro()
        }
    }

    private fun showTimerView(pomodoroStatus: PomodoroStatus? = null) {

        timerLayout.visible()
        selectionLayout.gone()

        val balanceTime = pomodoroStatus?.balanceTime ?: 0
        countDownView.text = Utils.getDurationBreakdown(balanceTime)

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

        val startButtonTextString: String
        val modeTextString: String
        val timerFullTime: Long?

        when (pomodoroStatus?.pomodoroState ?: PomodoroState.Focus) {
            PomodoroState.Focus -> {
                startButtonTextString = getString(R.string.start_focus)
                modeTextString = getString(R.string.focus)
                timerFullTime = timerPreference?.getFocusTime()
            }
            PomodoroState.ShortBreak -> {
                startButtonTextString = getString(R.string.take_short_break)
                modeTextString = getString(R.string.short_break)
                timerFullTime = timerPreference?.getShortBreakTime()
            }
            PomodoroState.LongBreak -> {
                startButtonTextString = getString(R.string.take_long_break)
                modeTextString = getString(R.string.long_break)
                timerFullTime = timerPreference?.getLongBreakTime()
            }
        }

        modeText.text = modeTextString
        startButton.text = startButtonTextString
        timerProgressBar.progress = Utils.getProportionalProgress(timerFullTime ?: 0, balanceTime)

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
