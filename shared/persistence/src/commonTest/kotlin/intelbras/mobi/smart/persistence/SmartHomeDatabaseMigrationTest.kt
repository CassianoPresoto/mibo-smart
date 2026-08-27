package intelbras.mobi.smart.persistence

import intelbras.mobi.smart.domain.capture.model.CameraCapture
import intelbras.mobi.smart.domain.preferences.model.UserPreference
import intelbras.mobi.smart.persistence.db.SmartHomeDatabase
import intelbras.mobi.smart.persistence.capture.StoredCameraCaptures
import intelbras.mobi.smart.persistence.preferences.StoredUserPreferences
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest

class SmartHomeDatabaseMigrationTest {

    @Test
    fun `the schema moved to the version that knows the camera captures`() {
        assertEquals(3L, SmartHomeDatabase.Schema.version)
    }

    @Test
    fun `upgrading a version 2 database opens room for the camera captures`() = runTest {
        val driver = versionTwoDriver()
        SmartHomeDatabase.Schema.migrate(driver, oldVersion = 2, newVersion = 3)
        val captures = StoredCameraCaptures(SmartHomeDatabase(driver), Dispatchers.Default)

        captures.save(photo)

        assertEquals(listOf("capture-1"), captures.capturesOf("KAYK0109140D9").first().map { it.id })
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

    private val photo = CameraCapture.Photo(
        id = "capture-1",
        deviceSerialNumber = "KAYK0109140D9",
        fileName = "foto.jpg",
        capturedAtEpochMilliseconds = 1_724_589_000_000L,
        sizeBytes = 838_860L,
    )

    private fun versionTwoDriver() = versionOneDriver().also { driver ->
        driver.execute(
            identifier = null,
            sql = "CREATE TABLE userPreferenceEntity (" +
                "preferenceKey TEXT NOT NULL PRIMARY KEY, " +
                "preferenceValue TEXT NOT NULL)",
            parameters = 0,
        )
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
