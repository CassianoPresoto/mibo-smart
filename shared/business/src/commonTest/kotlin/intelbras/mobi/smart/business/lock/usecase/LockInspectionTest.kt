package intelbras.mobi.smart.business.lock.usecase

import dev.mokkery.answering.returns
import dev.mokkery.answering.throws
import dev.mokkery.everySuspend
import dev.mokkery.matcher.any
import dev.mokkery.mock
import dev.mokkery.verifySuspend
import intelbras.mobi.smart.domain.device.model.DeviceReference
import intelbras.mobi.smart.domain.lock.LockRepository
import intelbras.mobi.smart.domain.lock.model.LockOpeningStatus
import intelbras.mobi.smart.rest.SmartHomeNetworkException
import intelbras.mobi.smart.rest.SmartHomeNotFoundException
import intelbras.mobi.smart.rest.SmartHomeServerException
import intelbras.mobi.smart.rest.SmartHomeUnauthorizedException
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs
import kotlinx.coroutines.test.runTest

class LockInspectionTest {

    private val lock = DeviceReference(
        serialNumber = "08B95FFFFE02116A_OGQ0010782013_sqNzDUSq",
        productId = "3Y2FSCDJ",
    )

    @Test
    fun `tells that the lock is closed`() = runTest {
        val result = LockInspection(repositoryAnswering(isOpen = false))(lock)

        assertEquals(LockStatusResult.Known(isOpen = false), result)
    }

    @Test
    fun `tells that the lock is open`() = runTest {
        val result = LockInspection(repositoryAnswering(isOpen = true))(lock)

        assertEquals(LockStatusResult.Known(isOpen = true), result)
    }

    @Test
    fun `asks about the lock the caller chose`() = runTest {
        val lockRepository = repositoryAnswering(isOpen = false)

        LockInspection(lockRepository)(lock)

        verifySuspend { lockRepository.readOpeningStatus(lock) }
    }

    @Test
    fun `a lock the platform cannot reach is reported as offline`() = runTest {
        val result = inspectionFailingWith(SmartHomeNotFoundException("HTTP 404"))

        assertEquals(LockStatusResult.DeviceOffline, result)
    }

    @Test
    fun `a refused token becomes the invalid token case`() = runTest {
        val result = inspectionFailingWith(SmartHomeUnauthorizedException("HTTP 401"))

        assertEquals(LockStatusResult.InvalidToken, result)
    }

    @Test
    fun `a network failure becomes the offline case`() = runTest {
        val result = inspectionFailingWith(SmartHomeNetworkException())

        assertEquals(LockStatusResult.NetworkUnavailable, result)
    }

    @Test
    fun `any other failure is reported with its cause`() = runTest {
        val cause = SmartHomeServerException("HTTP 503")

        val result = inspectionFailingWith(cause)

        assertEquals(cause, assertIs<LockStatusResult.Error>(result).cause)
    }

    private suspend fun inspectionFailingWith(failure: Throwable): LockStatusResult {
        val lockRepository = mock<LockRepository> {
            everySuspend { readOpeningStatus(any()) } throws failure
        }
        return LockInspection(lockRepository)(lock)
    }

    private fun repositoryAnswering(isOpen: Boolean) = mock<LockRepository> {
        everySuspend { readOpeningStatus(any()) } returns LockOpeningStatus(isOpen = isOpen)
    }
}
