package intelbras.mobi.smart.business

import intelbras.mobi.smart.business.usecase.ConnectionTermination
import intelbras.mobi.smart.business.usecase.DeviceConnecting
import intelbras.mobi.smart.business.usecase.DeviceConnection
import intelbras.mobi.smart.business.usecase.DeviceConnectionResult
import intelbras.mobi.smart.business.usecase.DisconnectionResult
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
