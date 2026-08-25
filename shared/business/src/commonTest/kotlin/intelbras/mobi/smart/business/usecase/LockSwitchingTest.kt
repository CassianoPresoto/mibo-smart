package intelbras.mobi.smart.business.usecase

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
import intelbras.mobi.smart.domain.lock.model.LockControlRequest
import intelbras.mobi.smart.domain.lock.model.LockOpeningStatus
import intelbras.mobi.smart.rest.SmartHomeNetworkException
import intelbras.mobi.smart.rest.SmartHomeNotFoundException
import intelbras.mobi.smart.rest.SmartHomeOperationRejectedException
import intelbras.mobi.smart.rest.SmartHomeServerException
import intelbras.mobi.smart.rest.SmartHomeUnauthorizedException
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs
import kotlin.time.Duration.Companion.seconds
import kotlinx.coroutines.test.runTest

class LockSwitchingTest {

    private val lock = DeviceReference(
        serialNumber = "08B95FFFFE02116A_OGQ0010782013_sqNzDUSq",
        productId = "3Y2FSCDJ",
    )

    @Test
    fun `asks the platform to open the lock`() = runTest {
        val lockRepository = acceptingRepository(reportsOpen = true)

        val result = switching(lockRepository)(lock, open = true)

        assertEquals(LockOperationResult.Done(isOpen = true, confirmed = true), result)
        verifySuspend {
            lockRepository.control(
                LockControlRequest(
                    serialNumber = "08B95FFFFE02116A_OGQ0010782013_sqNzDUSq",
                    productId = "3Y2FSCDJ",
                    open = true,
                )
            )
        }
    }

    @Test
    fun `asks the platform to close the lock`() = runTest {
        val lockRepository = acceptingRepository(reportsOpen = false)

        switching(lockRepository)(lock, open = false)

        verifySuspend {
            lockRepository.control(
                LockControlRequest(
                    serialNumber = "08B95FFFFE02116A_OGQ0010782013_sqNzDUSq",
                    productId = "3Y2FSCDJ",
                    open = false,
                )
            )
        }
    }

    @Test
    fun `a command the platform refuses is reported as refused`() = runTest {
        val result = switchingFailingWith(SmartHomeOperationRejectedException("recusado"))

        assertEquals(LockOperationResult.Refused, result)
    }

    @Test
    fun `a lock the platform cannot reach is reported as offline`() = runTest {
        val result = switchingFailingWith(SmartHomeNotFoundException("HTTP 404"))

        assertEquals(LockOperationResult.DeviceOffline, result)
    }

    @Test
    fun `a refused token becomes the invalid token case`() = runTest {
        val result = switchingFailingWith(SmartHomeUnauthorizedException("HTTP 401"))

        assertEquals(LockOperationResult.InvalidToken, result)
    }

    @Test
    fun `a network failure becomes the offline case`() = runTest {
        val result = switchingFailingWith(SmartHomeNetworkException())

        assertEquals(LockOperationResult.NetworkUnavailable, result)
    }

    @Test
    fun `any other failure is reported with its cause`() = runTest {
        val cause = SmartHomeServerException("HTTP 503")

        val result = switchingFailingWith(cause)

        assertEquals(cause, assertIs<LockOperationResult.Error>(result).cause)
    }

    @Test
    fun `waits for the lock to report the state that was asked`() = runTest {
        val lockRepository = mock<LockRepository> {
            everySuspend { control(any()) } returns Unit
            everySuspend { readOpeningStatus(any()) } sequentially {
                returns(LockOpeningStatus(isOpen = false))
                returns(LockOpeningStatus(isOpen = true))
            }
        }

        val result = switching(lockRepository)(lock, open = true)

        assertEquals(LockOperationResult.Done(isOpen = true, confirmed = true), result)
        verifySuspend(VerifyMode.exhaustiveOrder) {
            lockRepository.control(any())
            lockRepository.readOpeningStatus(lock)
            lockRepository.readOpeningStatus(lock)
        }
    }

    @Test
    fun `a lock that never reports the change is left unconfirmed`() = runTest {
        val lockRepository = acceptingRepository(reportsOpen = false)

        val result = switching(lockRepository)(lock, open = true)

        assertEquals(LockOperationResult.Done(isOpen = false, confirmed = false), result)
    }

    @Test
    fun `a lock that cannot be read after the command is left unconfirmed`() = runTest {
        val lockRepository = mock<LockRepository> {
            everySuspend { control(any()) } returns Unit
            everySuspend { readOpeningStatus(any()) } throws SmartHomeNetworkException()
        }

        val result = switching(lockRepository)(lock, open = true)

        assertEquals(LockOperationResult.Done(isOpen = true, confirmed = false), result)
    }

    private suspend fun switchingFailingWith(failure: Throwable): LockOperationResult {
        val lockRepository = mock<LockRepository> {
            everySuspend { control(any()) } throws failure
        }
        return switching(lockRepository)(lock, open = true)
    }

    private fun switching(lockRepository: LockRepository) =
        LockSwitching(lockRepository, LockConfirmationPolicy(attempts = 2, firstWait = 1.seconds))

    private fun acceptingRepository(reportsOpen: Boolean) = mock<LockRepository> {
        everySuspend { control(any()) } returns Unit
        everySuspend { readOpeningStatus(any()) } returns LockOpeningStatus(isOpen = reportsOpen)
    }
}
