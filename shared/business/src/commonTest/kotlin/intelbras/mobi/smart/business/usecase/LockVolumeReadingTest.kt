package intelbras.mobi.smart.business.usecase

import dev.mokkery.answering.returns
import dev.mokkery.answering.throws
import dev.mokkery.everySuspend
import dev.mokkery.matcher.any
import dev.mokkery.mock
import dev.mokkery.verifySuspend
import intelbras.mobi.smart.domain.device.model.DeviceReference
import intelbras.mobi.smart.domain.lock.LockRepository
import intelbras.mobi.smart.domain.lock.model.LockVolumeLevel
import intelbras.mobi.smart.domain.lock.model.LockVolumeStatus
import intelbras.mobi.smart.domain.preferences.UserPreferenceStore
import intelbras.mobi.smart.domain.preferences.model.UserPreference
import intelbras.mobi.smart.rest.SmartHomeNetworkException
import intelbras.mobi.smart.rest.SmartHomeNotFoundException
import intelbras.mobi.smart.rest.SmartHomeServerException
import intelbras.mobi.smart.rest.SmartHomeUnauthorizedException
import intelbras.mobi.smart.rest.SmartHomeUnknownPlatformErrorException
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs
import kotlinx.coroutines.test.runTest

class LockVolumeReadingTest {

    private val lock = DeviceReference(
        serialNumber = "08B95FFFFE02116A_OGQ0010782013_sqNzDUSq",
        productId = "3Y2FSCDJ",
    )

    @Test
    fun `tells the volume the lock reports`() = runTest {
        val result = reading(repositoryAnswering(LockVolumeLevel.Medium))(lock)

        assertEquals(LockVolumeResult.Known(LockVolumeLevel.Medium), result)
    }

    @Test
    fun `tells that the lock is muted`() = runTest {
        val result = reading(repositoryAnswering(LockVolumeLevel.Mute))(lock)

        assertEquals(LockVolumeResult.Known(LockVolumeLevel.Mute), result)
    }

    @Test
    fun `asks about the lock the caller chose`() = runTest {
        val lockRepository = repositoryAnswering(LockVolumeLevel.High)

        reading(lockRepository)(lock)

        verifySuspend { lockRepository.readVolume(lock) }
    }

    @Test
    fun `a lock the platform cannot reach is reported as offline`() = runTest {
        val result = readingFailingWith(SmartHomeNotFoundException("HTTP 404"))

        assertEquals(LockVolumeResult.DeviceOffline, result)
    }

    @Test
    fun `a refused token becomes the invalid token case`() = runTest {
        val result = readingFailingWith(SmartHomeUnauthorizedException("HTTP 401"))

        assertEquals(LockVolumeResult.InvalidToken, result)
    }

    @Test
    fun `a network failure becomes the network unavailable case`() = runTest {
        val result = readingFailingWith(SmartHomeNetworkException())

        assertEquals(LockVolumeResult.NetworkUnavailable, result)
    }

    @Test
    fun `a platform that cannot answer falls back to the level this app sent last`() = runTest {
        val result = readingFailingWith(
            failure = SmartHomeUnknownPlatformErrorException("HTTP 500: Erro desconhecido"),
            remembered = LockVolumeLevel.Low.name,
        )

        assertEquals(LockVolumeResult.Remembered(LockVolumeLevel.Low), result)
    }

    @Test
    fun `a lock this app never changed starts at the medium level`() = runTest {
        val result = readingFailingWith(
            failure = SmartHomeUnknownPlatformErrorException("HTTP 500: Erro desconhecido"),
            remembered = null,
        )

        assertEquals(LockVolumeResult.Remembered(LockVolumeLevel.Medium), result)
    }

    @Test
    fun `any other failure is reported with its cause`() = runTest {
        val cause = SmartHomeServerException("HTTP 503")

        val result = readingFailingWith(cause)

        assertEquals(cause, assertIs<LockVolumeResult.Error>(result).cause)
    }

    private suspend fun readingFailingWith(
        failure: Throwable,
        remembered: String? = null,
    ): LockVolumeResult {
        val lockRepository = mock<LockRepository> {
            everySuspend { readVolume(any()) } throws failure
        }
        return reading(lockRepository, remembered)(lock)
    }

    private fun reading(lockRepository: LockRepository, remembered: String? = null) =
        LockVolumeReading(lockRepository, LockVolumeMemory(preferencesHolding(remembered)))

    private fun preferencesHolding(remembered: String?) = mock<UserPreferenceStore> {
        everySuspend { read(UserPreference.LockVolume, any()) } returns remembered
    }

    private fun repositoryAnswering(level: LockVolumeLevel) = mock<LockRepository> {
        everySuspend { readVolume(any()) } returns LockVolumeStatus(volume = level)
    }
}
