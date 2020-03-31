package com.mymeetings.pairpomodoro.view

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.preference.PreferenceManager
import com.mymeetings.pairpomodoro.PomodoroService
import com.mymeetings.pairpomodoro.R
import com.mymeetings.pairpomodoro.model.PomoState
import com.mymeetings.pairpomodoro.model.PomodoroStatus
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

        pomodoroViewModel.pomodoroStatusLiveData.observe(this, Observer {
            if (it != null) {
                showTimerView(it)
            } else {
                showSelectionView()
            }
        })

        pomodoroViewModel.syncFailedLiveData.observe(this, Observer {
            ViewUtils.infoDialog(this, "Timer not available for this key") {
                PomodoroService.stopPomodoro(this)
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
            PomodoroService.createOwnPomodoro(this)
        }

        syncButton.setOnClickListener {
            ViewUtils.buildInputDialog(this) {
                PomodoroService.syncPomodoro(this, it)
                showTimerView(null)
            }
        }

        closeButton.setOnClickListener {
            ViewUtils.confirmationDialog(this, getString(R.string.exit_app)) {
                if (it) {
                    PomodoroService.stopPomodoro(this)
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

    private fun showSelectionView() {
        selectionLayout.visible()
        timerLayout.gone()
        container.keepScreenOn = false
    }
}
