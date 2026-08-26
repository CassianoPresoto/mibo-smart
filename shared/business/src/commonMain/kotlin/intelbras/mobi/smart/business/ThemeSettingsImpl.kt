package intelbras.mobi.smart.business

import intelbras.mobi.smart.domain.preferences.UserPreferenceStore
import intelbras.mobi.smart.domain.preferences.model.ThemeMode
import intelbras.mobi.smart.domain.preferences.model.UserPreference
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

internal class ThemeSettingsImpl(
    private val userPreferenceStore: UserPreferenceStore,
) : ThemeSettings {

    private val mutableMode = MutableStateFlow(ThemeMode.System)
    private val loading = Mutex()
    private var loaded = false

    override val mode: Flow<ThemeMode> = mutableMode.onStart { loadOnce() }

    override suspend fun choose(mode: ThemeMode) {
        loading.withLock { loaded = true }
        mutableMode.value = mode
        userPreferenceStore.save(UserPreference.ThemeMode, mode.name)
    }

    private suspend fun loadOnce() = loading.withLock {
        if (loaded) return@withLock
        loaded = true
        mutableMode.value = storedMode()
    }

    private suspend fun storedMode(): ThemeMode {
        val stored = userPreferenceStore.read(UserPreference.ThemeMode) ?: return ThemeMode.System
        return ThemeMode.entries.firstOrNull { it.name == stored } ?: ThemeMode.System
    }
}
