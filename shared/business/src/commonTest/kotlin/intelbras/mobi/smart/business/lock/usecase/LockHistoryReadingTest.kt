package intelbras.mobi.smart.business.lock.usecase

import dev.mokkery.answering.returns
import dev.mokkery.answering.throws
import dev.mokkery.everySuspend
import dev.mokkery.matcher.any
import dev.mokkery.mock
import dev.mokkery.verifySuspend
import intelbras.mobi.smart.domain.device.model.DeviceReference
import intelbras.mobi.smart.domain.lock.LockRepository
import intelbras.mobi.smart.domain.lock.model.LockHistoryRequest
import intelbras.mobi.smart.domain.lock.model.LockOpeningRecord
import intelbras.mobi.smart.rest.SmartHomeNetworkException
import intelbras.mobi.smart.rest.SmartHomeNotFoundException
import intelbras.mobi.smart.rest.SmartHomeServerException
import intelbras.mobi.smart.rest.SmartHomeUnauthorizedException
import intelbras.mobi.smart.rest.SmartHomeUnknownPlatformErrorException
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs
import kotlin.test.assertNull
import kotlinx.coroutines.test.runTest
import kotlinx.datetime.LocalDateTime

private const val HISTORY_LIMIT = 20

class LockHistoryReadingTest {

    private val lock = DeviceReference(
        serialNumber = "08B95FFFFE02116A_OGQ0010782013_sqNzDUSq",
        productId = "3Y2FSCDJ",
    )

    @Test
    fun `asks the platform for the amount of records the caller wants`() = runTest {
        val lockRepository = repositoryAnswering(emptyList())

        LockHistoryReading(lockRepository)(lock, HISTORY_LIMIT)

        verifySuspend {
            lockRepository.readOpeningHistory(
                LockHistoryRequest(
                    serialNumber = "08B95FFFFE02116A_OGQ0010782013_sqNzDUSq",
                    limit = HISTORY_LIMIT,
                )
            )
        }
    }

    @Test
    fun `reads the local time the platform reports`() = runTest {
        val result = readingOf(
            LockOpeningRecord(localTime = "20260825T172107", user = "APP", way = "usuarioRemoto")
        )

        val opening = assertIs<LockHistoryResult.Loaded>(result).openings.single()
        assertEquals(LocalDateTime(2026, 8, 25, 17, 21, 7), opening.happenedAt)
        assertEquals("APP", opening.user)
        assertEquals(LockOpeningWay.RemoteApp, opening.way)
    }

    @Test
    fun `keeps the name of a way it does not recognize`() = runTest {
        val result = readingOf(
            LockOpeningRecord(localTime = "20260825T172107", user = "Cassiano", way = "senha")
        )

        val opening = assertIs<LockHistoryResult.Loaded>(result).openings.single()
        assertEquals(LockOpeningWay.Unrecognized("senha"), opening.way)
    }

    @Test
    fun `a record without a readable time is kept without one`() = runTest {
        val result = readingOf(
            LockOpeningRecord(localTime = "ontem de tarde", user = "APP", way = "usuarioRemoto")
        )

        val opening = assertIs<LockHistoryResult.Loaded>(result).openings.single()
        assertNull(opening.happenedAt)
        assertEquals("APP", opening.user)
    }

    @Test
    fun `a lock without openings is reported as an empty history`() = runTest {
        val result = LockHistoryReading(repositoryAnswering(emptyList()))(lock, HISTORY_LIMIT)

        assertEquals(emptyList(), assertIs<LockHistoryResult.Loaded>(result).openings)
    }

    @Test
    fun `a platform that cannot answer for the lock reports the history as unavailable`() =
        runTest {
            val result = readingFailingWith(
                SmartHomeUnknownPlatformErrorException("HTTP 500: Erro desconhecido")
            )

            assertEquals(LockHistoryResult.Unavailable, result)
        }

    @Test
    fun `a lock the platform cannot reach is reported as offline`() = runTest {
        val result = readingFailingWith(SmartHomeNotFoundException("HTTP 404"))

        assertEquals(LockHistoryResult.DeviceOffline, result)
    }

    @Test
    fun `a refused token becomes the invalid token case`() = runTest {
        val result = readingFailingWith(SmartHomeUnauthorizedException("HTTP 401"))

        assertEquals(LockHistoryResult.InvalidToken, result)
    }

    @Test
    fun `a network failure becomes the network unavailable case`() = runTest {
        val result = readingFailingWith(SmartHomeNetworkException())

        assertEquals(LockHistoryResult.NetworkUnavailable, result)
    }

    @Test
    fun `any other failure is reported with its cause`() = runTest {
        val cause = SmartHomeServerException("HTTP 503")

        val result = readingFailingWith(cause)

        assertEquals(cause, assertIs<LockHistoryResult.Error>(result).cause)
    }

    private suspend fun readingOf(vararg records: LockOpeningRecord): LockHistoryResult =
        LockHistoryReading(repositoryAnswering(records.toList()))(lock, HISTORY_LIMIT)

    private suspend fun readingFailingWith(failure: Throwable): LockHistoryResult {
        val lockRepository = mock<LockRepository> {
            everySuspend { readOpeningHistory(any()) } throws failure
        }
        return LockHistoryReading(lockRepository)(lock, HISTORY_LIMIT)
    }

    private fun repositoryAnswering(records: List<LockOpeningRecord>) = mock<LockRepository> {
        everySuspend { readOpeningHistory(any()) } returns records
    }
}
