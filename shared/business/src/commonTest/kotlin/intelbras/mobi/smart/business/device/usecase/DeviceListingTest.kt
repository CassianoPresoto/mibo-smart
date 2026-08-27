package intelbras.mobi.smart.business.device.usecase

import dev.mokkery.answering.returns
import dev.mokkery.answering.throws
import dev.mokkery.everySuspend
import dev.mokkery.matcher.any
import dev.mokkery.mock
import dev.mokkery.verify.VerifyMode.Companion.not
import dev.mokkery.verifySuspend
import intelbras.mobi.smart.business.emptyPage
import intelbras.mobi.smart.domain.device.DeviceRepository
import intelbras.mobi.smart.domain.device.model.Device
import intelbras.mobi.smart.domain.device.model.DeviceCapabilities
import intelbras.mobi.smart.domain.device.model.DeviceKind
import intelbras.mobi.smart.domain.device.model.DeviceListPage
import intelbras.mobi.smart.domain.device.model.DeviceListQuery
import intelbras.mobi.smart.domain.device.model.DeviceOriginFilter
import intelbras.mobi.smart.domain.lock.LockRepository
import intelbras.mobi.smart.domain.lock.model.LockOpeningStatus
import intelbras.mobi.smart.rest.SmartHomeNetworkException
import intelbras.mobi.smart.rest.SmartHomeServerException
import intelbras.mobi.smart.rest.SmartHomeUnauthorizedException
import intelbras.mobi.smart.business.noLock
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs
import kotlinx.coroutines.test.runTest

class DeviceListingTest {

    private val camera = Device(serialNumber = "KAYK0109140D9")

    private val hub = Device(serialNumber = "OGQ0010782013", model = "IOT-ZG2-IB")

    private val lock = Device(
        serialNumber = "08B95FFFFE02116A",
        productId = "3Y2FSCDJ",
        isSubdevice = true,
        hubSerialNumber = "OGQ0010782013",
        hubProductId = "sqNzDUSq",
    )

    private val page = pageOf(camera)

    @Test
    fun `forwards filter and paging to the repository`() = runTest {
        val deviceRepository = repositoryReturning(page)

        listing(deviceRepository)(origin = DeviceOriginFilter.Linked, page = 3, pageSize = 30)

        verifySuspend {
            deviceRepository.listDevices(
                DeviceListQuery(page = 3, pageSize = 30, origin = DeviceOriginFilter.Linked)
            )
        }
    }

    @Test
    fun `page below the first one is corrected`() = runTest {
        val deviceRepository = repositoryReturning(page)

        listing(deviceRepository)(page = 0)

        verifySuspend {
            deviceRepository.listDevices(
                DeviceListQuery(
                    page = DeviceListQuery.FIRST_PAGE,
                    pageSize = DeviceListQuery.DEFAULT_PAGE_SIZE,
                )
            )
        }
    }

    @Test
    fun `page size out of range is capped`() = runTest {
        val deviceRepository = repositoryReturning(page)

        listing(deviceRepository)(pageSize = 500)

        verifySuspend {
            deviceRepository.listDevices(
                DeviceListQuery(page = DeviceListQuery.FIRST_PAGE, pageSize = 100)
            )
        }
    }

    @Test
    fun `every device comes with the kind it announces`() = runTest {
        val deviceRepository = repositoryReturning(pageOf(camera, lock))
        everySuspend { deviceRepository.readCapabilities(lock.serial()) } returns DeviceCapabilities("")

        val result = listing(deviceRepository, answeringLock())()

        assertEquals(
            listOf(
                CatalogDevice(camera, DeviceKind.Camera),
                CatalogDevice(lock, DeviceKind.Lock),
            ),
            assertIs<DeviceListResult.Success>(result).devices,
        )
    }

    @Test
    fun `a device that others hang on is the hub of the page`() = runTest {
        val deviceRepository = repositoryReturning(pageOf(hub, lock))
        everySuspend { deviceRepository.readCapabilities(lock.serial()) } returns DeviceCapabilities("")

        val result = listing(deviceRepository, answeringLock())()

        assertEquals(
            listOf(CatalogDevice(hub, DeviceKind.Hub), CatalogDevice(lock, DeviceKind.Lock)),
            assertIs<DeviceListResult.Success>(result).devices,
        )
        verifySuspend(not) { deviceRepository.readCapabilities(hub.serial()) }
    }

    @Test
    fun `asks the capabilities of each device that was listed`() = runTest {
        val deviceRepository = repositoryReturning(pageOf(camera, lock))

        listing(deviceRepository)()

        verifySuspend { deviceRepository.readCapabilities(camera.serial()) }
        verifySuspend { deviceRepository.readCapabilities(lock.serial()) }
    }

    @Test
    fun `a device whose capabilities cannot be read still shows up`() = runTest {
        val deviceRepository = repositoryReturning(page)
        everySuspend {
            deviceRepository.readCapabilities(any())
        } throws SmartHomeServerException("HTTP 503")

        val result = listing(deviceRepository)()

        assertEquals(
            listOf(CatalogDevice(camera, DeviceKind.Unknown)),
            assertIs<DeviceListResult.Success>(result).devices,
        )
    }

    @Test
    fun `no device on the first page becomes the empty case`() = runTest {
        val result = listing(repositoryReturning(emptyPage()))()

        assertEquals(DeviceListResult.Empty, result)
    }

    @Test
    fun `a refused token becomes the invalid token case`() = runTest {
        val result = listing(repositoryFailingWith(SmartHomeUnauthorizedException("HTTP 401")))()

        assertEquals(DeviceListResult.InvalidToken, result)
    }

    @Test
    fun `a network failure becomes the offline case`() = runTest {
        val result = listing(repositoryFailingWith(SmartHomeNetworkException()))()

        assertEquals(DeviceListResult.NetworkUnavailable, result)
    }

    @Test
    fun `any other failure is reported with its cause`() = runTest {
        val cause = SmartHomeServerException("HTTP 503")

        val result = listing(repositoryFailingWith(cause))()

        assertEquals(cause, assertIs<DeviceListResult.Error>(result).cause)
    }

    private fun listing(
        deviceRepository: DeviceRepository,
        lockRepository: LockRepository = noLock(),
    ) = DeviceListing(deviceRepository, DeviceKindResolution(deviceRepository, lockRepository))

    private fun answeringLock() = mock<LockRepository> {
        everySuspend { readOpeningStatus(any()) } returns LockOpeningStatus(isOpen = false)
    }

    private fun pageOf(vararg devices: Device) = DeviceListPage(
        page = DeviceListQuery.FIRST_PAGE,
        pageSize = DeviceListQuery.DEFAULT_PAGE_SIZE,
        origin = DeviceOriginFilter.All,
        devices = devices.toList(),
    )

    private fun repositoryReturning(page: DeviceListPage) = mock<DeviceRepository> {
        everySuspend { listDevices(any()) } returns page
        everySuspend { readCapabilities(any()) } returns DeviceCapabilities("RTSV1,AudioTalk")
    }

    private fun repositoryFailingWith(failure: Throwable) = mock<DeviceRepository> {
        everySuspend { listDevices(any()) } throws failure
    }
}
