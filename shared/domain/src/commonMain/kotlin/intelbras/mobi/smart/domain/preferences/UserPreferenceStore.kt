package intelbras.mobi.smart.domain.preferences

import intelbras.mobi.smart.domain.preferences.model.UserPreference

interface UserPreferenceStore {
    suspend fun read(preference: UserPreference): String?

    suspend fun save(preference: UserPreference, value: String)

    suspend fun clear(preference: UserPreference)
}
