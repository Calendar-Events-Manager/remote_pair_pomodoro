package com.mymeetings.pairpomodoro.model.pomodoroTimer

import kotlinx.coroutines.*
import kotlinx.coroutines.channels.ReceiveChannel
import kotlinx.coroutines.channels.ticker
import java.util.concurrent.TimeUnit

class TickerRunner {

    private var job: Job? = null
    private val delay = TimeUnit.SECONDS.toMillis(1)

    fun run(timeTicker: Ticker) {
        cancelPrevious()
        val delay = TimeUnit.SECONDS.toMillis(1)
        val tickerChannel = ticker(
            delayMillis = delay,
            initialDelayMillis = delay
        )
        consumer(timeTicker, tickerChannel)
    }

    fun cancel() {
        if (job?.isActive == true) {
            job?.cancel()
        }
    }

    private fun cancelPrevious() {
        cancel()
    }

    private fun consumer(tickerTimer: Ticker, tickerChannel: ReceiveChannel<Unit>) {

        job = CoroutineScope(Dispatchers.Default).launch {
            while (isActive) {
                tickerChannel.receive()
                if (isActive) {
                    tickerTimer.tick(delay)
                }
            }
        }
    }
}