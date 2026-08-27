package intelbras.mobi.smart.business.device.usecase

import dev.mokkery.answering.returns
import dev.mokkery.answering.throws
import dev.mokkery.everySuspend
import dev.mokkery.matcher.any
import dev.mokkery.mock
import dev.mokkery.verify.VerifyMode.Companion.not
import dev.mokkery.verifySuspend
import intelbras.mobi.smart.domain.device.DeviceRepository
import intelbras.mobi.smart.domain.device.model.Device
import intelbras.mobi.smart.domain.device.model.DeviceCapabilities
import intelbras.mobi.smart.domain.device.model.DeviceKind
import intelbras.mobi.smart.domain.device.model.DeviceReference
import intelbras.mobi.smart.domain.device.model.DeviceSerial
import intelbras.mobi.smart.domain.lock.LockRepository
import intelbras.mobi.smart.domain.lock.model.LockOpeningStatus
import intelbras.mobi.smart.rest.SmartHomeNetworkException
import intelbras.mobi.smart.rest.SmartHomeNotFoundException
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlinx.coroutines.test.runTest

class DeviceKindResolutionTest {

    private val camera = Device(serialNumber = "KAYK0109140D9", model = "iM3-C")

    private val lock = Device(
        serialNumber = "08B95FFFFE02116A",
        model = "MFR 2020 V",
        productId = "3Y2FSCDJ",
        isSubdevice = true,
        hubSerialNumber = "OGQ0010782013",
        hubProductId = "sqNzDUSq",
    )

    private val hub = Device(serialNumber = "OGQ0010782013", model = "IOT-ZG2-IB")

    @Test
    fun `a device that streams live video is a camera`() = runTest {
        assertEquals(DeviceKind.Camera, resolve(camera, capabilities = "RTSV1,AudioTalk"))
    }

    @Test
    fun `a newer version of the streaming capability is still a camera`() = runTest {
        assertEquals(DeviceKind.Camera, resolve(camera, capabilities = "rtsv2"))
    }

    @Test
    fun `a subdevice that answers about its opening is a lock`() = runTest {
        assertEquals(DeviceKind.Lock, resolve(lock, capabilities = ""))
    }

    @Test
    fun `the lock is asked through the address the hub gives it`() = runTest {
        val lockRepository = lockRepositoryThatAnswers()

        resolution(capabilities = "", lockRepository = lockRepository)(lock)

        verifySuspend {
            lockRepository.readOpeningStatus(
                DeviceReference(
                    serialNumber = "08B95FFFFE02116A_OGQ0010782013_sqNzDUSq",
                    productId = "3Y2FSCDJ",
                )
            )
        }
    }

    @Test
    fun `a subdevice that does not answer about its opening has no kind`() = runTest {
        val lockRepository = lockRepositoryFailingWith(SmartHomeNotFoundException("HTTP 404"))

        val kind = resolution(capabilities = "", lockRepository = lockRepository)(lock)

        assertEquals(DeviceKind.Unknown, kind)
    }

    @Test
    fun `a hub announces nothing and is not asked about locks`() = runTest {
        val lockRepository = lockRepositoryThatAnswers()

        val kind = resolution(capabilities = "", lockRepository = lockRepository)(hub)

        assertEquals(DeviceKind.Unknown, kind)
        verifySuspend(not) { lockRepository.readOpeningStatus(any()) }
    }

    @Test
    fun `what the device announces is enough to spot a camera`() = runTest {
        val kind = resolution(capabilities = "RTSV1").announcedKind(DeviceSerial("KAYK0109140D9"))

        assertEquals(DeviceKind.Camera, kind)
    }

    @Test
    fun `a failure reading the capabilities is left for the caller to decide`() = runTest {
        val deviceRepository = mock<DeviceRepository> {
            everySuspend { readCapabilities(any()) } throws SmartHomeNetworkException()
        }

        assertFailsWith<SmartHomeNetworkException> {
            DeviceKindResolution(deviceRepository, lockRepositoryThatAnswers())(camera)
        }
    }

    private suspend fun resolve(device: Device, capabilities: String) =
        resolution(capabilities)(device)

    private fun resolution(
        capabilities: String,
        lockRepository: LockRepository = lockRepositoryThatAnswers(),
    ): DeviceKindResolution {
        val deviceRepository = mock<DeviceRepository> {
            everySuspend { readCapabilities(any()) } returns DeviceCapabilities(capabilities)
        }
        return DeviceKindResolution(deviceRepository, lockRepository)
    }

    private fun lockRepositoryThatAnswers() = mock<LockRepository> {
        everySuspend { readOpeningStatus(any()) } returns LockOpeningStatus(isOpen = false)
    }

    private fun lockRepositoryFailingWith(failure: Throwable) = mock<LockRepository> {
        everySuspend { readOpeningStatus(any()) } throws failure
    }
}
