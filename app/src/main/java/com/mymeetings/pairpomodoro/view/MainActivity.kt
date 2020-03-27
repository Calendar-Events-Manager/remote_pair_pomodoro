package com.mymeetings.pairpomodoro.view

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import com.mymeetings.pairpomodoro.PomodorService
import com.mymeetings.pairpomodoro.R
import com.mymeetings.pairpomodoro.model.PomoState
import com.mymeetings.pairpomodoro.model.PomodoroStatus
import com.mymeetings.pairpomodoro.model.timerAlarm.AndroidTimerAlarm
import com.mymeetings.pairpomodoro.model.timerPreference.UserTimerPreference
import com.mymeetings.pairpomodoro.utils.Utils
import com.mymeetings.pairpomodoro.view.preference.PreferenceActivity
import com.mymeetings.pairpomodoro.viewmodel.PomodoroViewModel
import kotlinx.android.synthetic.main.activity_main.*


class MainActivity : AppCompatActivity() {

    private lateinit var pomodoroViewModel: PomodoroViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        pomodoroViewModel = ViewModelProvider(this).get(PomodoroViewModel::class.java)

        showSelectionView()
        pomodoroViewModel.pomodoroStatusLiveData.observe(this, Observer {
            if (it != null) {
                showTimerView(it)
            } else {
                showSelectionView()
            }
        })

        pauseButton.setOnClickListener {
            pomodoroViewModel.pause()
        }

        startButton.setOnClickListener {
            pomodoroViewModel.start()
        }

        resetButton.setOnClickListener {
            ViewUtils.confirmationDialog(this, getString(R.string.reset_timer)) {
                if (it) {
                    pomodoroViewModel.reset()
                }
            }
        }

        createButton.setOnClickListener {
            pomodoroViewModel.createOwnPomodoro(
                timerAlarm = AndroidTimerAlarm(this),
                timerPreference = UserTimerPreference(this)
            )
            showTimerView(null)
        }

        syncButton.setOnClickListener {
            ViewUtils.buildInputDialog(this) {
                pomodoroViewModel.syncPomodoro(it, AndroidTimerAlarm(this))
                showTimerView(null)
            }
        }

        closeButton.setOnClickListener {
            ViewUtils.confirmationDialog(this, getString(R.string.exit_app)) {
                if (it) {
                    pomodoroViewModel.close()
                }
            }
        }

        sharingKeyText.setOnClickListener {
            ViewUtils.copyTextToClipBoard(this, sharingKeyText.text.toString())
        }

        settingsButton.setOnClickListener {
            PreferenceActivity.open(this)
        }
    }

    private fun showTimerView(pomodoroStatus: PomodoroStatus?) {

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

        sharingKeyText.text = pomodoroViewModel.sharingKey

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
        startService()
    }

    private fun showSelectionView() {
        selectionLayout.visible()
        timerLayout.gone()
        stopService()
    }

    fun startService() {
        val serviceIntent = Intent(this, PomodorService::class.java)
        serviceIntent.putExtra("inputExtra", "Foreground Service Example in Android")
        ContextCompat.startForegroundService(this, serviceIntent)
    }

    fun stopService() {
        val serviceIntent = Intent(this, PomodorService::class.java)
        stopService(serviceIntent)
    }
}