package intelbras.mobi.smart.business.activity.usecase

import dev.mokkery.answering.calls
import dev.mokkery.answering.returns
import dev.mokkery.answering.throws
import dev.mokkery.everySuspend
import dev.mokkery.matcher.any
import dev.mokkery.mock
import intelbras.mobi.smart.business.device.usecase.DeviceKindResolution
import intelbras.mobi.smart.business.device.usecase.DeviceListing
import intelbras.mobi.smart.business.lock.usecase.LockHistoryReading
import intelbras.mobi.smart.domain.device.DeviceRepository
import intelbras.mobi.smart.domain.device.model.Device
import intelbras.mobi.smart.domain.device.model.DeviceCapabilities
import intelbras.mobi.smart.domain.device.model.DeviceListPage
import intelbras.mobi.smart.domain.device.model.DeviceOriginFilter
import intelbras.mobi.smart.domain.device.model.DeviceReference
import intelbras.mobi.smart.domain.lock.LockRepository
import intelbras.mobi.smart.domain.lock.model.LockHistoryRequest
import intelbras.mobi.smart.domain.lock.model.LockOpeningRecord
import intelbras.mobi.smart.domain.lock.model.LockOpeningStatus
import intelbras.mobi.smart.rest.SmartHomeNetworkException
import intelbras.mobi.smart.rest.SmartHomeNotFoundException
import intelbras.mobi.smart.rest.SmartHomeUnauthorizedException
import intelbras.mobi.smart.rest.SmartHomeUnknownPlatformErrorException
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs
import kotlin.test.assertTrue
import kotlinx.coroutines.test.runTest

private const val LIMIT = 20

class HomeActivityReadingTest {

    @Test
    fun `brings the openings of every lock in one timeline`() = runTest {
        val result = reading(
            devices = listOf(lock("porta", "Porta de entrada"), lock("garagem", "Garagem")),
            openings = mapOf(
                "porta" to listOf(record("20260826T081200")),
                "garagem" to listOf(record("20260826T073800")),
            ),
        )(LIMIT)

        val entries = assertIs<HomeActivityResult.Loaded>(result).entries
        assertEquals(listOf("Porta de entrada", "Garagem"), entries.map { it.lockName })
    }

    @Test
    fun `shows the most recent opening first`() = runTest {
        val result = reading(
            devices = listOf(lock("porta", "Porta"), lock("garagem", "Garagem")),
            openings = mapOf(
                "porta" to listOf(record("20260825T090000")),
                "garagem" to listOf(record("20260826T190000")),
            ),
        )(LIMIT)

        val entries = assertIs<HomeActivityResult.Loaded>(result).entries
        assertEquals(listOf("Garagem", "Porta"), entries.map { it.lockName })
    }

    @Test
    fun `a lock whose history failed does not hide the openings of the others`() = runTest {
        val result = reading(
            devices = listOf(lock("porta", "Porta"), lock("garagem", "Garagem")),
            openings = mapOf("porta" to listOf(record("20260826T081200"))),
            failingLock = "garagem",
        )(LIMIT)

        val entries = assertIs<HomeActivityResult.Loaded>(result).entries
        assertEquals(listOf("Porta"), entries.map { it.lockName })
    }

    @Test
    fun `an account without locks says so`() = runTest {
        val result = reading(devices = listOf(camera()), openings = emptyMap())(LIMIT)

        assertEquals(HomeActivityResult.NoLocks, result)
    }

    @Test
    fun `an account without devices says there are no locks`() = runTest {
        val result = reading(devices = emptyList(), openings = emptyMap())(LIMIT)

        assertEquals(HomeActivityResult.NoLocks, result)
    }

    @Test
    fun `a platform that answers for no lock is reported as unavailable`() = runTest {
        val result = reading(
            devices = listOf(lock("porta", "Porta")),
            openings = emptyMap(),
            failingLock = "porta",
        )(LIMIT)

        assertEquals(HomeActivityResult.Unavailable, result)
    }

    @Test
    fun `asks each lock for the amount of records the caller wants`() = runTest {
        var requestedLimit = 0
        val lockRepository = lockRepository(
            openings = mapOf("porta" to listOf(record("20260826T081200"))),
            onHistory = { request -> requestedLimit = request.limit },
        )

        readingWith(devices = listOf(lock("porta", "Porta")), lockRepository = lockRepository)(LIMIT)

        assertEquals(LIMIT, requestedLimit)
    }

    @Test
    fun `a refused token becomes the invalid token case`() = runTest {
        val deviceRepository = mock<DeviceRepository> {
            everySuspend { listDevices(any()) } throws SmartHomeUnauthorizedException("HTTP 401")
        }

        assertEquals(HomeActivityResult.InvalidToken, readingWith(deviceRepository)(LIMIT))
    }

    @Test
    fun `a network failure while listing the devices is reported`() = runTest {
        val deviceRepository = mock<DeviceRepository> {
            everySuspend { listDevices(any()) } throws SmartHomeNetworkException()
        }

        assertEquals(HomeActivityResult.NetworkUnavailable, readingWith(deviceRepository)(LIMIT))
    }

    @Test
    fun `locks without any opening make an empty timeline`() = runTest {
        val result = reading(
            devices = listOf(lock("porta", "Porta")),
            openings = emptyMap(),
        )(LIMIT)

        assertTrue(assertIs<HomeActivityResult.Loaded>(result).entries.isEmpty())
    }

    private fun record(localTime: String) =
        LockOpeningRecord(localTime = localTime, user = "APP", way = "usuarioRemoto")

    private fun lock(serial: String, name: String) = Device(
        serialNumber = serial,
        name = name,
        model = "MFR 2020 V",
        productId = "produto-$serial",
        isSubdevice = true,
        hubSerialNumber = "hub",
        hubProductId = "produto-hub",
    )

    private fun camera() = Device(
        serialNumber = "camera",
        name = "Câmera",
        model = "iM3-C",
    )

    private fun reading(
        devices: List<Device>,
        openings: Map<String, List<LockOpeningRecord>>,
        failingLock: String? = null,
    ) = readingWith(
        devices = devices,
        lockRepository = lockRepository(openings, failingLock),
    )

    private fun lockRepository(
        openings: Map<String, List<LockOpeningRecord>>,
        failingLock: String? = null,
        onHistory: (LockHistoryRequest) -> Unit = {},
    ) = mock<LockRepository> {
        everySuspend { readOpeningStatus(any()) } calls { (reference: DeviceReference) ->
            if (reference.serialNumber.startsWith("camera")) {
                throw SmartHomeNotFoundException("HTTP 404")
            }
            LockOpeningStatus(isOpen = false)
        }
        everySuspend { readOpeningHistory(any()) } calls { (request: LockHistoryRequest) ->
            onHistory(request)
            if (failingLock != null && request.serialNumber.startsWith(failingLock)) {
                throw SmartHomeUnknownPlatformErrorException("HTTP 500: Erro desconhecido")
            }
            openings.entries
                .firstOrNull { (serial, _) -> request.serialNumber.startsWith(serial) }
                ?.value
                .orEmpty()
        }
    }

    private fun readingWith(
        deviceRepository: DeviceRepository,
        lockRepository: LockRepository = mock {
            everySuspend { readOpeningHistory(any()) } returns emptyList()
        },
    ) = HomeActivityReading(
        DeviceListing(deviceRepository, DeviceKindResolution(deviceRepository, lockRepository)),
        LockHistoryReading(lockRepository),
    )

    private fun readingWith(devices: List<Device>, lockRepository: LockRepository) = readingWith(
        deviceRepository = mock {
            everySuspend { listDevices(any()) } returns DeviceListPage(
                page = 1,
                pageSize = 50,
                origin = DeviceOriginFilter.All,
                devices = devices,
            )
            everySuspend { readCapabilities(any()) } returns DeviceCapabilities()
        },
        lockRepository = lockRepository,
    )
}
