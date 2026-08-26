package intelbras.mobi.smart.business

import intelbras.mobi.smart.domain.preferences.UserPreferenceStore
import intelbras.mobi.smart.domain.preferences.model.ThemeMode
import intelbras.mobi.smart.domain.preferences.model.UserPreference
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals

class ThemeSettingsTest {

    @Test
    fun `follows the system while the user has not chosen`() = runTest {
        val settings = ThemeSettingsImpl(InMemoryUserPreferences())

        assertEquals(ThemeMode.System, settings.mode.first())
    }

    @Test
    fun `starts on the theme chosen in a previous run`() = runTest {
        val store = InMemoryUserPreferences(mapOf(UserPreference.ThemeMode to "Dark"))

        assertEquals(ThemeMode.Dark, ThemeSettingsImpl(store).mode.first())
    }

    @Test
    fun `falls back to the system when the stored value is not a theme`() = runTest {
        val store = InMemoryUserPreferences(mapOf(UserPreference.ThemeMode to "roxo"))

        assertEquals(ThemeMode.System, ThemeSettingsImpl(store).mode.first())
    }

    @Test
    fun `publishes the theme the user just chose`() = runTest {
        val settings = ThemeSettingsImpl(InMemoryUserPreferences())

        settings.choose(ThemeMode.Dark)

        assertEquals(ThemeMode.Dark, settings.mode.first())
    }

    @Test
    fun `keeps the chosen theme for the next run`() = runTest {
        val store = InMemoryUserPreferences()

        ThemeSettingsImpl(store).choose(ThemeMode.Light)

        assertEquals("Light", store.read(UserPreference.ThemeMode))
    }

    @Test
    fun `a choice made before the first read is not overwritten by the stored value`() = runTest {
        val store = InMemoryUserPreferences(mapOf(UserPreference.ThemeMode to "Dark"))
        val settings = ThemeSettingsImpl(store)

        settings.choose(ThemeMode.Light)

        assertEquals(ThemeMode.Light, settings.mode.first())
    }
}

private class InMemoryUserPreferences(
    initial: Map<UserPreference, String> = emptyMap(),
) : UserPreferenceStore {

    private val values = initial.toMutableMap()

    override suspend fun read(preference: UserPreference): String? = values[preference]

    override suspend fun save(preference: UserPreference, value: String) {
        values[preference] = value
    }

    override suspend fun clear(preference: UserPreference) {
        values.remove(preference)
    }
}
