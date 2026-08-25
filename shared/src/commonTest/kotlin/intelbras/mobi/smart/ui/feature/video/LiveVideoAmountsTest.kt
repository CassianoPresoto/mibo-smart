package intelbras.mobi.smart.ui.feature.video

import kotlin.test.Test
import kotlin.test.assertEquals

class LiveVideoAmountsTest {

    @Test
    fun `bytes are shown as megabytes with one decimal`() {
        assertEquals("5,0", megabytesOf(5_242_880L))
        assertEquals("1,5", megabytesOf(1_572_864L))
        assertEquals("0,0", megabytesOf(0L))
    }

    @Test
    fun `quota keeps a single decimal`() {
        assertEquals("1,0", withOneDecimal(1.0))
        assertEquals("0,8", withOneDecimal(0.84))
        assertEquals("2,5", withOneDecimal(2.46))
        assertEquals("3,0", withOneDecimal(2.97))
    }
}
