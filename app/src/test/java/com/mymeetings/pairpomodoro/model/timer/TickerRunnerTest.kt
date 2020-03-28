package com.mymeetings.pairpomodoro.model.timer

import io.mockk.MockKAnnotations
import io.mockk.impl.annotations.MockK
import io.mockk.verify
import kotlinx.coroutines.delay
import kotlinx.coroutines.runBlocking
import org.junit.Before
import org.junit.Test

class TickerRunnerTest {

    private lateinit var tickerRunner: TickerRunner

    @MockK
    private lateinit var ticker: Ticker

    @Before
    fun setup() {
        MockKAnnotations.init(this, relaxUnitFun = true)
        tickerRunner = TickerRunner()
    }

    @Test
    fun `run should call tick for each second`() = runBlocking {
        tickerRunner.run(ticker)

        delay(2000)
        verify(exactly = 2) { ticker.tick() }
    }

    @Test
    fun `cancel should prevent calling tick for each second`() = runBlocking {
        tickerRunner.run(ticker)
        delay(1000)
        tickerRunner.cancel()

        verify(exactly = 1) { ticker.tick() }
    }
}