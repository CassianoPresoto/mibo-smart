package intelbras.mobi.smart.business.device

import dev.mokkery.answering.returns
import dev.mokkery.everySuspend
import dev.mokkery.matcher.any
import dev.mokkery.mock
import intelbras.mobi.smart.business.device.usecase.CatalogDevice
import intelbras.mobi.smart.business.device.usecase.DeviceKindResolution
import intelbras.mobi.smart.business.device.usecase.DeviceListResult
import intelbras.mobi.smart.business.device.usecase.DeviceListing
import intelbras.mobi.smart.business.noLock
import intelbras.mobi.smart.domain.device.DeviceRepository
import intelbras.mobi.smart.domain.device.model.Device
import intelbras.mobi.smart.domain.device.model.DeviceCapabilities
import intelbras.mobi.smart.domain.device.model.DeviceKind
import intelbras.mobi.smart.domain.device.model.DeviceListPage
import intelbras.mobi.smart.domain.device.model.DeviceOriginFilter
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlinx.coroutines.test.runTest

class DeviceCatalogTest {

    private val page = DeviceListPage(
        page = 1,
        pageSize = 20,
        origin = DeviceOriginFilter.All,
        devices = listOf(Device(serialNumber = "KAYK0109140D9")),
    )

    @Test
    fun `lists the devices of the requested origin`() = runTest {
        val deviceRepository = mock<DeviceRepository> {
            everySuspend { listDevices(any()) } returns page
            everySuspend { readCapabilities(any()) } returns DeviceCapabilities("RTSV1")
        }
        val catalog: DeviceCatalog = DeviceCatalogImpl(
            DeviceListing(deviceRepository, DeviceKindResolution(deviceRepository, noLock())),
        )

        val result = catalog.listDevices(origin = DeviceOriginFilter.Linked)

        assertEquals(
            DeviceListResult.Success(listOf(CatalogDevice(page.devices.single(), DeviceKind.Camera))),
            result,
        )
    }
}
