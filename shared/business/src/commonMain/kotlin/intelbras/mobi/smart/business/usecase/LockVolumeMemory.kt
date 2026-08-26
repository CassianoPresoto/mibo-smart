package intelbras.mobi.smart.business.usecase

import intelbras.mobi.smart.domain.device.model.DeviceReference
import intelbras.mobi.smart.domain.lock.model.LockVolumeLevel
import intelbras.mobi.smart.domain.preferences.UserPreferenceStore
import intelbras.mobi.smart.domain.preferences.model.UserPreference

internal class LockVolumeMemory(
    private val userPreferenceStore: UserPreferenceStore,
) {

    suspend fun lastLevelOf(lock: DeviceReference): LockVolumeLevel {
        val stored = userPreferenceStore.read(UserPreference.LockVolume, lock.preferenceScope())
        return LockVolumeLevel.entries.firstOrNull { it.name == stored } ?: DEFAULT_LEVEL
    }

    suspend fun remember(lock: DeviceReference, level: LockVolumeLevel) =
        userPreferenceStore.save(
            preference = UserPreference.LockVolume,
            value = level.name,
            scope = lock.preferenceScope(),
        )

    private fun DeviceReference.preferenceScope() = "$serialNumber|$productId"

    private companion object {
        val DEFAULT_LEVEL = LockVolumeLevel.Medium
    }
}
