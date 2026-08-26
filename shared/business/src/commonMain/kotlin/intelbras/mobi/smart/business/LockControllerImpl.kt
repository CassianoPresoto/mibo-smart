package intelbras.mobi.smart.business

import intelbras.mobi.smart.business.usecase.LockDetails
import intelbras.mobi.smart.business.usecase.LockDetailsReading
import intelbras.mobi.smart.business.usecase.LockHistoryReading
import intelbras.mobi.smart.business.usecase.LockHistoryResult
import intelbras.mobi.smart.business.usecase.LockInspection
import intelbras.mobi.smart.business.usecase.LockOperationResult
import intelbras.mobi.smart.business.usecase.LockStatusResult
import intelbras.mobi.smart.business.usecase.LockSwitching
import intelbras.mobi.smart.business.usecase.LockVolumeChangeResult
import intelbras.mobi.smart.business.usecase.LockVolumeChanging
import intelbras.mobi.smart.business.usecase.LockVolumeReading
import intelbras.mobi.smart.business.usecase.LockVolumeResult
import intelbras.mobi.smart.domain.device.model.DeviceReference
import intelbras.mobi.smart.domain.lock.model.LockVolumeLevel

internal class LockControllerImpl(
    private val lockInspection: LockInspection,
    private val lockSwitching: LockSwitching,
    private val lockVolumeReading: LockVolumeReading,
    private val lockVolumeChanging: LockVolumeChanging,
    private val lockHistoryReading: LockHistoryReading,
    private val lockDetailsReading: LockDetailsReading,
) : LockController {

    override suspend fun statusOf(lock: DeviceReference): LockStatusResult = lockInspection(lock)

    override suspend fun switch(lock: DeviceReference, open: Boolean): LockOperationResult =
        lockSwitching(lock, open)

    override suspend fun volumeOf(lock: DeviceReference): LockVolumeResult =
        lockVolumeReading(lock)

    override suspend fun changeVolume(
        lock: DeviceReference,
        level: LockVolumeLevel,
    ): LockVolumeChangeResult = lockVolumeChanging(lock, level)

    override suspend fun historyOf(lock: DeviceReference, limit: Int): LockHistoryResult =
        lockHistoryReading(lock, limit)

    override suspend fun detailsOf(lock: DeviceReference): LockDetails = lockDetailsReading(lock)
}
