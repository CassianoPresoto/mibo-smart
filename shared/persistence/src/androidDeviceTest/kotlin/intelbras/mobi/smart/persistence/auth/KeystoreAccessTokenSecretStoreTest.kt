package intelbras.mobi.smart.persistence.auth

import androidx.test.platform.app.InstrumentationRegistry
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue

class KeystoreAccessTokenSecretStoreTest {

    private val context = InstrumentationRegistry.getInstrumentation().targetContext
    private val store = KeystoreAccessTokenSecretStore(context)

    @BeforeTest
    fun setUp() = store.clear()

    @AfterTest
    fun tearDown() = store.clear()

    @Test
    fun readsNothingBeforeAnythingIsWritten() {
        assertNull(store.read())
    }

    @Test
    fun readsBackWhatItWrote() {
        store.write("Ot_token")

        assertEquals("Ot_token", store.read())
    }

    @Test
    fun writingAgainReplacesThePreviousSecret() {
        store.write("Ot_first")
        store.write("Ot_second")

        assertEquals("Ot_second", store.read())
    }

    @Test
    fun clearingRemovesTheSecret() {
        store.write("Ot_token")

        store.clear()

        assertNull(store.read())
    }

    @Test
    fun keepsTheTokenUnreadableOnDisk() {
        store.write("Ot_token")

        val storedValue = context
            .getSharedPreferences("smart_home_secrets", android.content.Context.MODE_PRIVATE)
            .getString("access_token", null)

        assertNotNull(storedValue)
        assertTrue(storedValue.contains(":"), "esperava iv e texto cifrado separados")
        assertTrue("Ot_token" !in storedValue, "o token não pode aparecer em claro")
    }
}
