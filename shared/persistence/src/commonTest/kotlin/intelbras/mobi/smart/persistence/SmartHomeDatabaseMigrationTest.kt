package intelbras.mobi.smart.persistence

import intelbras.mobi.smart.domain.preferences.model.UserPreference
import intelbras.mobi.smart.persistence.db.SmartHomeDatabase
import intelbras.mobi.smart.persistence.preferences.StoredUserPreferences
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.test.runTest

class SmartHomeDatabaseMigrationTest {

    @Test
    fun `the schema moved to the version that knows the preferences`() {
        assertEquals(2L, SmartHomeDatabase.Schema.version)
    }

    @Test
    fun `upgrading a version 1 database opens room for the preferences`() = runTest {
        val driver = versionOneDriver()
        SmartHomeDatabase.Schema.migrate(driver, oldVersion = 1, newVersion = 2)
        val store = StoredUserPreferences(SmartHomeDatabase(driver), Dispatchers.Default)

        store.save(UserPreference.ThemeMode, "Dark")

        assertEquals("Dark", store.read(UserPreference.ThemeMode))
    }

    @Test
    fun `upgrading a version 1 database keeps the session that was already there`() = runTest {
        val driver = versionOneDriver()
        val database = SmartHomeDatabase(driver)
        database.accessTokenQueries.replaceExpiration(id = 1L, expiresAtEpochMilliseconds = 42L)

        SmartHomeDatabase.Schema.migrate(driver, oldVersion = 1, newVersion = 2)

        assertEquals(42L, database.accessTokenQueries.selectExpiration(1L).executeAsOne())
    }

    private fun versionOneDriver() = emptyDriver().also { driver ->
        driver.execute(
            identifier = null,
            sql = "CREATE TABLE accessTokenEntity (" +
                "id INTEGER NOT NULL PRIMARY KEY, " +
                "expiresAtEpochMilliseconds INTEGER NOT NULL)",
            parameters = 0,
        )
    }
}
