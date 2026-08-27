package intelbras.mobi.smart.business.device

import intelbras.mobi.smart.business.device.usecase.ConnectionTermination
import intelbras.mobi.smart.business.device.usecase.DeviceConnecting
import intelbras.mobi.smart.business.device.usecase.DeviceConnection
import intelbras.mobi.smart.business.device.usecase.DeviceConnectionResult
import intelbras.mobi.smart.business.device.usecase.DisconnectionResult
import intelbras.mobi.smart.domain.device.model.DeviceReference

internal class DeviceConnectorImpl(
    private val deviceConnecting: DeviceConnecting,
    private val connectionTermination: ConnectionTermination,
) : DeviceConnector {

    override suspend fun connect(device: DeviceReference): DeviceConnectionResult =
        deviceConnecting(device)

    override suspend fun disconnect(connection: DeviceConnection): DisconnectionResult =
        connectionTermination(connection)
}
