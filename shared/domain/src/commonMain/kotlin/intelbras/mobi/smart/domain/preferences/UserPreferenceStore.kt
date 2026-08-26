package intelbras.mobi.smart.domain.preferences

import intelbras.mobi.smart.domain.preferences.model.UserPreference

interface UserPreferenceStore {
    suspend fun read(preference: UserPreference, scope: String? = null): String?

    suspend fun save(preference: UserPreference, value: String, scope: String? = null)

    suspend fun clear(preference: UserPreference, scope: String? = null)
}
