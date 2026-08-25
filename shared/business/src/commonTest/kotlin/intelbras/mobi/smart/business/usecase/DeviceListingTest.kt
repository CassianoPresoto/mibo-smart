package intelbras.mobi.smart.business.usecase

import dev.mokkery.answering.returns
import dev.mokkery.answering.throws
import dev.mokkery.everySuspend
import dev.mokkery.matcher.any
import dev.mokkery.mock
import dev.mokkery.verifySuspend
import intelbras.mobi.smart.business.emptyPage
import intelbras.mobi.smart.domain.device.DeviceRepository
import intelbras.mobi.smart.domain.device.model.Device
import intelbras.mobi.smart.domain.device.model.DeviceListPage
import intelbras.mobi.smart.domain.device.model.DeviceListQuery
import intelbras.mobi.smart.domain.device.model.DeviceOriginFilter
import intelbras.mobi.smart.rest.SmartHomeNetworkException
import intelbras.mobi.smart.rest.SmartHomeServerException
import intelbras.mobi.smart.rest.SmartHomeUnauthorizedException
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs
import kotlinx.coroutines.test.runTest

class DeviceListingTest {

    private val page = DeviceListPage(
        page = 1,
        pageSize = 20,
        origin = DeviceOriginFilter.All,
        devices = listOf(Device(serialNumber = "KAYK0109140D9")),
    )

    @Test
    fun `forwards filter and paging to the repository`() = runTest {
        val deviceRepository = repositoryReturning(page)

        DeviceListing(deviceRepository)(origin = DeviceOriginFilter.Linked, page = 3, pageSize = 30)

        verifySuspend {
            deviceRepository.listDevices(
                DeviceListQuery(page = 3, pageSize = 30, origin = DeviceOriginFilter.Linked)
            )
        }
    }

    @Test
    fun `page below the first one is corrected`() = runTest {
        val deviceRepository = repositoryReturning(page)

        DeviceListing(deviceRepository)(page = 0)

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

        DeviceListing(deviceRepository)(pageSize = 500)

        verifySuspend {
            deviceRepository.listDevices(
                DeviceListQuery(page = DeviceListQuery.FIRST_PAGE, pageSize = 100)
            )
        }
    }

    @Test
    fun `devices found become a successful listing`() = runTest {
        val result = DeviceListing(repositoryReturning(page))()

        assertEquals(DeviceListResult.Success(page), result)
    }

    @Test
    fun `no device on the first page becomes the empty case`() = runTest {
        val result = DeviceListing(repositoryReturning(emptyPage()))()

        assertEquals(DeviceListResult.Empty, result)
    }

    @Test
    fun `a refused token becomes the invalid token case`() = runTest {
        val result = DeviceListing(repositoryFailingWith(SmartHomeUnauthorizedException("HTTP 401")))()

        assertEquals(DeviceListResult.InvalidToken, result)
    }

    @Test
    fun `a network failure becomes the offline case`() = runTest {
        val result = DeviceListing(repositoryFailingWith(SmartHomeNetworkException()))()

        assertEquals(DeviceListResult.NetworkUnavailable, result)
    }

    @Test
    fun `any other failure is reported with its cause`() = runTest {
        val cause = SmartHomeServerException("HTTP 503")

        val result = DeviceListing(repositoryFailingWith(cause))()

        assertEquals(cause, assertIs<DeviceListResult.Error>(result).cause)
    }

    private fun repositoryReturning(page: DeviceListPage) = mock<DeviceRepository> {
        everySuspend { listDevices(any()) } returns page
    }

    private fun repositoryFailingWith(failure: Throwable) = mock<DeviceRepository> {
        everySuspend { listDevices(any()) } throws failure
    }
}
