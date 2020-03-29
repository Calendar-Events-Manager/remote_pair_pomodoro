package com.mymeetings.pairpomodoro.model.pomodoroManager

import com.mymeetings.pairpomodoro.model.PomoState
import com.mymeetings.pairpomodoro.model.PomodoroStatus
import com.mymeetings.pairpomodoro.model.pomodoroAlarm.TimerAlarm
import com.mymeetings.pairpomodoro.model.pomodoroPreference.TimerPreference
import com.mymeetings.pairpomodoro.model.pomodoroSyncer.TimerSyncer
import io.mockk.*
import io.mockk.impl.annotations.MockK
import io.mockk.impl.annotations.RelaxedMockK
import org.junit.Before
import org.junit.Test

typealias CreationCallback = (TimerPreference) -> Unit
typealias UpdateCallback = (PomodoroStatus) -> Unit

class PomodoroManagerTest {

    private lateinit var pomodoroManager: PomodoroManager

    @MockK
    private lateinit var timerAlarm: TimerAlarm
    private val sharingKey = "ABCDE"
    @RelaxedMockK
    private lateinit var timerPreference: TimerPreference
    @MockK
    private lateinit var timerSyncer: TimerSyncer
    @MockK
    private lateinit var updateCallback: ((PomodoroStatus) -> Unit)

    @Before
    fun setup() {
        MockKAnnotations.init(this, relaxUnitFun = true)
        every { updateCallback.invoke(any()) } just Runs
        pomodoroManager = PomodoroManager(
            timerAlarm = timerAlarm,
            timerSyncer = timerSyncer,
            updateCallback = updateCallback
        )
    }

    @Test
    fun `create with Mode_CREATE should create pomodoro timer and listen to syncer for only status info`() {

        pomodoroManager.create(timerPreference)

        verify {
            timerSyncer.registerTimerUpdate(
                sharingKey = any(),
                statusCallback = any()
            )
        }
    }

    @Test
    fun `create with Mode_SYNC should listen to syncer for creation and status info and create timer from syncer`() {

        val capturedCreatedCallback = CapturingSlot<CreationCallback>()
        val capturedUpdateCallback = CapturingSlot<UpdateCallback>()

        val expectedStatus = PomodoroStatus(
            pomoState = PomoState.Focus,
            balanceTime = timerPreference.getFocusTime(),
            shortBreaksLeft = timerPreference.getShortBreakCount(),
            pause = false
        )

        every {
            timerSyncer.registerTimerUpdate(
                sharingKey = sharingKey,
                createdCallback = capture(capturedCreatedCallback),
                statusCallback = capture(capturedUpdateCallback)
            )
        } answers {
            capturedCreatedCallback.captured.invoke(timerPreference)
            capturedUpdateCallback.captured.invoke(expectedStatus)
        }

        pomodoroManager.sync(sharingKey)


        verify {
            timerSyncer.registerTimerUpdate(
                sharingKey = sharingKey,
                statusCallback = any(),
                createdCallback = any()
            )
            updateCallback.invoke(expectedStatus)
        }
        verify(exactly = 0) {
            timerSyncer.setTimerStatus(expectedStatus)
        }
    }

    @Test
    fun `start should start pomodoroTimer and update the syncer`() {

        pomodoroManager.create(timerPreference)
        pomodoroManager.start()

        val expectedStatus = PomodoroStatus(
            pomoState = PomoState.Focus,
            balanceTime = timerPreference.getFocusTime(),
            shortBreaksLeft = timerPreference.getShortBreakCount(),
            pause = false
        )

        verify {
            updateCallback.invoke(expectedStatus)
            timerSyncer.setTimerStatus(expectedStatus)
        }
    }

    @Test
    fun `pause should pause pomodoroTimer and update the syncer`() {

        pomodoroManager.create(timerPreference)
        pomodoroManager.pause()

        val expectedStatus = PomodoroStatus(
            pomoState = PomoState.Focus,
            balanceTime = timerPreference.getFocusTime(),
            shortBreaksLeft = timerPreference.getShortBreakCount(),
            pause = true
        )

        verify {
            updateCallback.invoke(expectedStatus)
            timerSyncer.setTimerStatus(expectedStatus)
        }
    }

    @Test
    fun `reset should reset pomodoroTimer and update the syncer`() {

        pomodoroManager.create(timerPreference)
        pomodoroManager.reset()

        val expectedStatus = PomodoroStatus(
            pomoState = PomoState.Focus,
            balanceTime = timerPreference.getFocusTime(),
            shortBreaksLeft = timerPreference.getShortBreakCount(),
            pause = true
        )

        verify {
            updateCallback.invoke(expectedStatus)
            timerSyncer.setTimerStatus(expectedStatus)
        }
    }

    @Test
    fun `close should unregister timerUpdate and reset pomodoro timer`() {

        pomodoroManager.create(timerPreference)
        pomodoroManager.close()

        verify {
            timerSyncer.unregisterTimerUpdate()
        }
        verify(exactly = 0) {
            updateCallback.invoke(any())
        }
    }

    @Test
    fun `getSharingKey should return sharing key registered in syncer`() {
        every { timerSyncer.getSharingKey() } returns sharingKey

        val actualSharingKey = pomodoroManager.getShareKey()

        assert(actualSharingKey == sharingKey)
    }

    @Test
    fun `initializing PomodoroManager should auto assign FirebaseSyncer`() {
        val pomodoroManager = PomodoroManager(
            timerAlarm = timerAlarm,
            updateCallback = updateCallback
        )

        assert(pomodoroManager != null)
    }
}