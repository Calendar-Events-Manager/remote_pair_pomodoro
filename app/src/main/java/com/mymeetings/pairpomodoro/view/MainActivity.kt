package com.mymeetings.pairpomodoro.view

import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import com.mymeetings.pairpomodoro.R
import com.mymeetings.pairpomodoro.Utils
import com.mymeetings.pairpomodoro.model.PomoState
import com.mymeetings.pairpomodoro.model.PomodoroMode
import com.mymeetings.pairpomodoro.model.PomodoroStatus
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
            pomodoroViewModel.reset()
        }

        createButton.setOnClickListener {
            pomodoroViewModel.createOwnPomodoro()
            showTimerView(null)
        }

        syncButton.setOnClickListener {
            ViewUtils.buildInputDialog(this) {
                pomodoroViewModel.syncPomodoro(it)
                showTimerView(null)
            }
        }

        closeButton.setOnClickListener {
            pomodoroViewModel.close()
        }

        //TODO start service too.
    }

    private fun showTimerView(pomodoroStatus: PomodoroStatus?) {

        timerLayout.visible()
        selectionLayout.gone()

        countDownView.text = Utils.getDurationBreakdown(pomodoroStatus?.balanceTime ?: 0)

        if (pomodoroStatus == null) {
            startButton.gone()
            pauseResetLayout.gone()
            progressBar.visible()
        } else if (pomodoroStatus.pause) {
            startButton.visible()
            pauseResetLayout.gone()
            progressBar.gone()
        } else {
            startButton.gone()
            pauseResetLayout.visible()
            progressBar.gone()
        }

        if (pomodoroViewModel.pomodoroMode == PomodoroMode.CREATED) {
            startButton.enable()
            pauseButton.enable()
            resetButton.enable()
        } else {
            startButton.disable()
            pauseButton.disable()
            resetButton.disable()
        }

        sharingKeyText.text = pomodoroViewModel.sharingKey

        val mode = when (pomodoroStatus?.pomoState ?: PomoState.Default) {
            PomoState.Focus -> {
                "Focus"
            }
            PomoState.LongBreak -> {
                "Long Break"
            }
            PomoState.ShortBreak -> {
                "Short Break"
            }
            PomoState.Default -> {
                "Focus"
            }
        }
        modeText.text = mode
    }

    private fun showSelectionView() {
        selectionLayout.visible()
        timerLayout.gone()
    }
}

private fun View.gone() {
    visibility = View.GONE
}

private fun View.enable() {
    isEnabled = true
}

private fun View.disable() {
    isEnabled = false
}

private fun View.visible() {
    visibility = View.VISIBLE
}
