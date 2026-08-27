package intelbras.mobi.smart.business.lock.usecase

import dev.mokkery.answering.returns
import dev.mokkery.answering.sequentially
import dev.mokkery.answering.throws
import dev.mokkery.everySuspend
import dev.mokkery.matcher.any
import dev.mokkery.mock
import dev.mokkery.verify.VerifyMode
import dev.mokkery.verifySuspend
import intelbras.mobi.smart.domain.device.model.DeviceReference
import intelbras.mobi.smart.domain.lock.LockRepository
import intelbras.mobi.smart.domain.lock.model.LockVolumeLevel
import intelbras.mobi.smart.domain.lock.model.LockVolumeRequest
import intelbras.mobi.smart.domain.lock.model.LockVolumeStatus
import intelbras.mobi.smart.domain.preferences.UserPreferenceStore
import intelbras.mobi.smart.domain.preferences.model.UserPreference
import intelbras.mobi.smart.rest.SmartHomeNetworkException
import intelbras.mobi.smart.rest.SmartHomeNotFoundException
import intelbras.mobi.smart.rest.SmartHomeOperationRejectedException
import intelbras.mobi.smart.rest.SmartHomeServerException
import intelbras.mobi.smart.rest.SmartHomeUnauthorizedException
import intelbras.mobi.smart.rest.SmartHomeUnknownPlatformErrorException
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs
import kotlin.time.Duration.Companion.seconds
import kotlinx.coroutines.test.runTest

class LockVolumeChangingTest {

    private val lock = DeviceReference(
        serialNumber = "08B95FFFFE02116A_OGQ0010782013_sqNzDUSq",
        productId = "3Y2FSCDJ",
    )

    @Test
    fun `asks the platform for the volume the caller chose`() = runTest {
        val lockRepository = acceptingRepository(reportsLevel = LockVolumeLevel.Low)

        val result = changing(lockRepository)(lock, LockVolumeLevel.Low)

        assertEquals(
            LockVolumeChangeResult.Done(level = LockVolumeLevel.Low, confirmed = true),
            result,
        )
        verifySuspend {
            lockRepository.changeVolume(
                LockVolumeRequest(
                    serialNumber = "08B95FFFFE02116A_OGQ0010782013_sqNzDUSq",
                    productId = "3Y2FSCDJ",
                    volume = LockVolumeLevel.Low,
                )
            )
        }
    }

    @Test
    fun `muting the lock sends the level the platform expects`() = runTest {
        val lockRepository = acceptingRepository(reportsLevel = LockVolumeLevel.Mute)

        changing(lockRepository)(lock, LockVolumeLevel.Mute)

        verifySuspend {
            lockRepository.changeVolume(
                LockVolumeRequest(
                    serialNumber = "08B95FFFFE02116A_OGQ0010782013_sqNzDUSq",
                    productId = "3Y2FSCDJ",
                    volume = LockVolumeLevel.Mute,
                )
            )
        }
    }

    @Test
    fun `waits for the lock to report the volume that was asked`() = runTest {
        val lockRepository = mock<LockRepository> {
            everySuspend { changeVolume(any()) } returns Unit
            everySuspend { readVolume(any()) } sequentially {
                returns(LockVolumeStatus(volume = LockVolumeLevel.Medium))
                returns(LockVolumeStatus(volume = LockVolumeLevel.High))
            }
        }

        val result = changing(lockRepository)(lock, LockVolumeLevel.High)

        assertEquals(
            LockVolumeChangeResult.Done(level = LockVolumeLevel.High, confirmed = true),
            result,
        )
        verifySuspend(VerifyMode.exhaustiveOrder) {
            lockRepository.changeVolume(any())
            lockRepository.readVolume(lock)
            lockRepository.readVolume(lock)
        }
    }

    @Test
    fun `a lock that never reports the new volume is left unconfirmed`() = runTest {
        val lockRepository = acceptingRepository(reportsLevel = LockVolumeLevel.Mute)

        val result = changing(lockRepository)(lock, LockVolumeLevel.High)

        assertEquals(
            LockVolumeChangeResult.Done(level = LockVolumeLevel.Mute, confirmed = false),
            result,
        )
    }

    @Test
    fun `a lock that cannot be read after the command is left unconfirmed`() = runTest {
        val lockRepository = mock<LockRepository> {
            everySuspend { changeVolume(any()) } returns Unit
            everySuspend { readVolume(any()) } throws SmartHomeNetworkException()
        }

        val result = changing(lockRepository)(lock, LockVolumeLevel.Low)

        assertEquals(
            LockVolumeChangeResult.Done(level = LockVolumeLevel.Low, confirmed = false),
            result,
        )
    }

    @Test
    fun `stops rereading when the platform says it cannot answer for the lock`() = runTest {
        val lockRepository = mock<LockRepository> {
            everySuspend { changeVolume(any()) } returns Unit
            everySuspend { readVolume(any()) } throws
                SmartHomeUnknownPlatformErrorException("HTTP 500: Erro desconhecido")
        }

        val result = changing(lockRepository)(lock, LockVolumeLevel.High)

        assertEquals(
            LockVolumeChangeResult.Done(level = LockVolumeLevel.High, confirmed = false),
            result,
        )
        verifySuspend(VerifyMode.exactly(1)) { lockRepository.readVolume(lock) }
    }

    @Test
    fun `a command the platform refuses is reported as refused`() = runTest {
        val result = changingFailingWith(SmartHomeOperationRejectedException("recusado"))

        assertEquals(LockVolumeChangeResult.Refused, result)
    }

    @Test
    fun `a lock the platform cannot reach is reported as offline`() = runTest {
        val result = changingFailingWith(SmartHomeNotFoundException("HTTP 404"))

        assertEquals(LockVolumeChangeResult.DeviceOffline, result)
    }

    @Test
    fun `a refused token becomes the invalid token case`() = runTest {
        val result = changingFailingWith(SmartHomeUnauthorizedException("HTTP 401"))

        assertEquals(LockVolumeChangeResult.InvalidToken, result)
    }

    @Test
    fun `a network failure becomes the network unavailable case`() = runTest {
        val result = changingFailingWith(SmartHomeNetworkException())

        assertEquals(LockVolumeChangeResult.NetworkUnavailable, result)
    }

    @Test
    fun `any other failure is reported with its cause`() = runTest {
        val cause = SmartHomeServerException("HTTP 503")

        val result = changingFailingWith(cause)

        assertEquals(cause, assertIs<LockVolumeChangeResult.Error>(result).cause)
    }

    private suspend fun changingFailingWith(failure: Throwable): LockVolumeChangeResult {
        val lockRepository = mock<LockRepository> {
            everySuspend { changeVolume(any()) } throws failure
        }
        return changing(lockRepository)(lock, LockVolumeLevel.High)
    }

    @Test
    fun `remembers the level it sent so the screen can show it later`() = runTest {
        val userPreferenceStore = emptyPreferences()

        changing(acceptingRepository(LockVolumeLevel.High), userPreferenceStore)(
            lock,
            LockVolumeLevel.High,
        )

        verifySuspend {
            userPreferenceStore.save(
                preference = UserPreference.LockVolume,
                value = "High",
                scope = "08B95FFFFE02116A_OGQ0010782013_sqNzDUSq|3Y2FSCDJ",
            )
        }
    }

    @Test
    fun `a level the platform refuses is not remembered`() = runTest {
        val userPreferenceStore = emptyPreferences()
        val lockRepository = mock<LockRepository> {
            everySuspend { changeVolume(any()) } throws
                SmartHomeOperationRejectedException("recusado")
        }

        changing(lockRepository, userPreferenceStore)(lock, LockVolumeLevel.High)

        verifySuspend(VerifyMode.not) { userPreferenceStore.save(any(), any(), any()) }
    }

    private fun changing(
        lockRepository: LockRepository,
        userPreferenceStore: UserPreferenceStore = emptyPreferences(),
    ) = LockVolumeChanging(
        lockRepository,
        LockConfirmation(LockConfirmationPolicy(attempts = 2, firstWait = 1.seconds)),
        LockVolumeMemory(userPreferenceStore),
    )

    private fun emptyPreferences() = mock<UserPreferenceStore> {
        everySuspend { read(any(), any()) } returns null
        everySuspend { save(any(), any(), any()) } returns Unit
    }

    private fun acceptingRepository(reportsLevel: LockVolumeLevel) = mock<LockRepository> {
        everySuspend { changeVolume(any()) } returns Unit
        everySuspend { readVolume(any()) } returns LockVolumeStatus(volume = reportsLevel)
    }
}
