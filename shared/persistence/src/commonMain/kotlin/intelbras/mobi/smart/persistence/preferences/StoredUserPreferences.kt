package intelbras.mobi.smart.persistence.preferences

import intelbras.mobi.smart.domain.preferences.UserPreferenceStore
import intelbras.mobi.smart.domain.preferences.model.UserPreference
import intelbras.mobi.smart.persistence.db.SmartHomeDatabase
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.withContext

internal class StoredUserPreferences(
    database: SmartHomeDatabase,
    private val ioDispatcher: CoroutineDispatcher,
) : UserPreferenceStore {

    private val queries = database.userPreferenceQueries

    override suspend fun read(preference: UserPreference): String? = withContext(ioDispatcher) {
        queries.selectValue(preference.key).executeAsOneOrNull()
    }

    override suspend fun save(preference: UserPreference, value: String): Unit =
        withContext(ioDispatcher) {
            queries.replaceValue(preferenceKey = preference.key, preferenceValue = value)
        }

    override suspend fun clear(preference: UserPreference): Unit = withContext(ioDispatcher) {
        queries.deleteValue(preference.key)
    }
}
