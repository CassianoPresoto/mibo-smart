package intelbras.mobi.smart.persistence.preferences

import intelbras.mobi.smart.domain.preferences.model.UserPreference
import intelbras.mobi.smart.persistence.testDatabase
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.test.runTest

class StoredUserPreferencesTest {

    private val store = StoredUserPreferences(testDatabase(), Dispatchers.Default)

    @Test
    fun `reads nothing before the user chooses anything`() = runTest {
        assertNull(store.read(UserPreference.ThemeMode))
    }

    @Test
    fun `reads back what was saved`() = runTest {
        store.save(UserPreference.ThemeMode, "Dark")

        assertEquals("Dark", store.read(UserPreference.ThemeMode))
    }

    @Test
    fun `saving again replaces the previous choice instead of adding a second one`() = runTest {
        store.save(UserPreference.ThemeMode, "Dark")
        store.save(UserPreference.ThemeMode, "Light")

        assertEquals("Light", store.read(UserPreference.ThemeMode))
    }

    @Test
    fun `clearing forgets the choice`() = runTest {
        store.save(UserPreference.ThemeMode, "Dark")

        store.clear(UserPreference.ThemeMode)

        assertNull(store.read(UserPreference.ThemeMode))
    }

    @Test
    fun `keeps one value per scope`() = runTest {
        store.save(UserPreference.LockVolume, "High", scope = "lockA")
        store.save(UserPreference.LockVolume, "Mute", scope = "lockB")

        assertEquals("High", store.read(UserPreference.LockVolume, scope = "lockA"))
        assertEquals("Mute", store.read(UserPreference.LockVolume, scope = "lockB"))
    }

    @Test
    fun `a scoped value does not answer for the preference without scope`() = runTest {
        store.save(UserPreference.LockVolume, "High", scope = "lockA")

        assertNull(store.read(UserPreference.LockVolume))
    }

    @Test
    fun `clearing a preference that was never saved is harmless`() = runTest {
        store.clear(UserPreference.ThemeMode)

        assertNull(store.read(UserPreference.ThemeMode))
    }
}
