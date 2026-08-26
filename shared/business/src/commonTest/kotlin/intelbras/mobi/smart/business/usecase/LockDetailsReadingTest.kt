package intelbras.mobi.smart.business.usecase

import dev.mokkery.answering.returns
import dev.mokkery.answering.throws
import dev.mokkery.everySuspend
import dev.mokkery.matcher.any
import dev.mokkery.mock
import intelbras.mobi.smart.domain.device.DeviceRepository
import intelbras.mobi.smart.domain.device.model.DeviceBatteryLevel
import intelbras.mobi.smart.domain.device.model.DeviceReference
import intelbras.mobi.smart.domain.lock.LockRepository
import intelbras.mobi.smart.domain.lock.model.RemoteOpeningStatus
import intelbras.mobi.smart.domain.sensor.SensorRepository
import intelbras.mobi.smart.domain.sensor.model.ZigbeeSignalStrength
import intelbras.mobi.smart.rest.SmartHomeNetworkException
import intelbras.mobi.smart.rest.SmartHomeUnknownPlatformErrorException
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull
import kotlinx.coroutines.test.runTest

class LockDetailsReadingTest {

    private val lock = DeviceReference(
        serialNumber = "08B95FFFFE02116A_OGQ0010782013_sqNzDUSq",
        productId = "3Y2FSCDJ",
    )

    @Test
    fun `brings what the platform knows about the lock`() = runTest {
        val details = reading()(lock)

        assertEquals(98, details.batteryPercentage)
        assertEquals(4, details.signalStrength)
        assertEquals(true, details.remoteOpeningEnabled)
    }

    @Test
    fun `a battery the platform cannot read does not hide the other details`() = runTest {
        val details = reading(
            deviceRepository = mock<DeviceRepository> {
                everySuspend { readBatteryLevel(any()) } throws SmartHomeNetworkException()
            }
        )(lock)

        assertNull(details.batteryPercentage)
        assertEquals(4, details.signalStrength)
        assertEquals(true, details.remoteOpeningEnabled)
    }

    @Test
    fun `a signal the platform cannot answer for becomes unknown`() = runTest {
        val details = reading(
            sensorRepository = mock<SensorRepository> {
                everySuspend { readZigbeeSignal(any()) } throws
                    SmartHomeUnknownPlatformErrorException("HTTP 500: Erro desconhecido")
            }
        )(lock)

        assertNull(details.signalStrength)
        assertEquals(98, details.batteryPercentage)
    }

    @Test
    fun `a lock that does not report remote opening keeps the rest of the details`() = runTest {
        val details = reading(
            lockRepository = mock<LockRepository> {
                everySuspend { readRemoteOpeningStatus(any()) } throws SmartHomeNetworkException()
            }
        )(lock)

        assertNull(details.remoteOpeningEnabled)
        assertEquals(98, details.batteryPercentage)
        assertEquals(4, details.signalStrength)
    }

    private fun reading(
        deviceRepository: DeviceRepository = mock {
            everySuspend { readBatteryLevel(any()) } returns DeviceBatteryLevel(percentage = 98)
        },
        sensorRepository: SensorRepository = mock {
            everySuspend { readZigbeeSignal(any()) } returns ZigbeeSignalStrength(strength = 4)
        },
        lockRepository: LockRepository = mock {
            everySuspend { readRemoteOpeningStatus(any()) } returns
                RemoteOpeningStatus(isEnabled = true)
        },
    ) = LockDetailsReading(deviceRepository, sensorRepository, lockRepository)
}
