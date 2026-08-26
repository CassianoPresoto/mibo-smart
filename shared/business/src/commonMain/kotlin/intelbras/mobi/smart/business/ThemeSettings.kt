package intelbras.mobi.smart.business

import intelbras.mobi.smart.domain.preferences.model.ThemeMode
import kotlinx.coroutines.flow.Flow

interface ThemeSettings {
    val mode: Flow<ThemeMode>

    suspend fun choose(mode: ThemeMode)
}
