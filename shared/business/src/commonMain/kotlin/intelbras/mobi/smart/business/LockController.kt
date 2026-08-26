package intelbras.mobi.smart.business

import intelbras.mobi.smart.business.usecase.LockDetails
import intelbras.mobi.smart.business.usecase.LockHistoryResult
import intelbras.mobi.smart.business.usecase.LockOperationResult
import intelbras.mobi.smart.business.usecase.LockStatusResult
import intelbras.mobi.smart.business.usecase.LockVolumeChangeResult
import intelbras.mobi.smart.business.usecase.LockVolumeResult
import intelbras.mobi.smart.domain.device.model.DeviceReference
import intelbras.mobi.smart.domain.lock.model.LockVolumeLevel

interface LockController {
    suspend fun statusOf(lock: DeviceReference): LockStatusResult

    suspend fun switch(lock: DeviceReference, open: Boolean): LockOperationResult

    suspend fun volumeOf(lock: DeviceReference): LockVolumeResult

    suspend fun changeVolume(lock: DeviceReference, level: LockVolumeLevel): LockVolumeChangeResult

    suspend fun historyOf(lock: DeviceReference, limit: Int): LockHistoryResult

    suspend fun detailsOf(lock: DeviceReference): LockDetails
}
