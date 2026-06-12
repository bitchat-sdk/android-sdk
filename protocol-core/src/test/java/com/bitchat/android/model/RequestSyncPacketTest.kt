package com.bitchat.android.model

import org.junit.Assert.assertArrayEquals
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Test

class RequestSyncPacketTest {

    @Test
    fun roundTrip() {
        val packet = RequestSyncPacket(p = 19, m = 1L shl 19, data = byteArrayOf(0x01, 0x02, 0x03))
        val decoded = RequestSyncPacket.decode(packet.encode())
        assertNotNull(decoded)
        assertEquals(19, decoded!!.p)
        assertEquals(1L shl 19, decoded.m)
        assertArrayEquals(byteArrayOf(0x01, 0x02, 0x03), decoded.data)
    }

    @Test
    fun decodeAcceptsMaxP() {
        val packet = RequestSyncPacket(p = RequestSyncPacket.MAX_P, m = 1024L, data = byteArrayOf(0x00))
        assertNotNull(RequestSyncPacket.decode(packet.encode()))
    }

    @Test
    fun decodeRejectsPAboveMax() {
        val packet = RequestSyncPacket(p = RequestSyncPacket.MAX_P + 1, m = 1024L, data = byteArrayOf(0x00))
        assertNull(RequestSyncPacket.decode(packet.encode()))
    }

    @Test
    fun decodeRejectsZeroP() {
        val packet = RequestSyncPacket(p = 0, m = 1024L, data = byteArrayOf(0x00))
        assertNull(RequestSyncPacket.decode(packet.encode()))
    }

    // ── Cross-language golden vectors ────────────────────────────────────────
    // These hexes are pinned across all four SDK ecosystems (Swift, Kotlin,
    // TypeScript, Python) and exported to spec-tests/fixtures/request_sync.json.

    @Test
    fun goldenVectorBasicRoundTrip() {
        val golden = "01000113020004000800000300050102030405"
        val packet = RequestSyncPacket(p = 19, m = 1L shl 19, data = byteArrayOf(1, 2, 3, 4, 5))
        assertEquals(golden, packet.encode().toHex())

        val decoded = RequestSyncPacket.decode(golden.hexToBytes())
        assertNotNull(decoded)
        assertEquals(19, decoded!!.p)
        assertEquals(1L shl 19, decoded.m)
        assertArrayEquals(byteArrayOf(1, 2, 3, 4, 5), decoded.data)
    }

    @Test
    fun goldenVectorMaxPRoundTrip() {
        val golden = "01000120020004ffffffff03000100"
        val packet = RequestSyncPacket(p = RequestSyncPacket.MAX_P, m = 0xFFFF_FFFFL, data = byteArrayOf(0x00))
        assertEquals(golden, packet.encode().toHex())
        assertNotNull(RequestSyncPacket.decode(golden.hexToBytes()))
    }

    @Test
    fun goldenVectorExtendedDecodes() {
        // Extended iOS TLVs (0x04 types, 0x05 since, 0x06 fragment filter) are
        // unknown to this decoder and must be skipped, not rejected.
        val golden = "0100010802000400000100030001ff0400010305000800000000000f4240060003616263"
        val decoded = RequestSyncPacket.decode(golden.hexToBytes())
        assertNotNull(decoded)
        assertEquals(8, decoded!!.p)
        assertEquals(256L, decoded.m)
        assertArrayEquals(byteArrayOf(0xFF.toByte()), decoded.data)
    }

    @Test
    fun goldenVectorUnknownTLVSkipped() {
        val golden = "7f0002beef01000113020004000800000300050102030405"
        val decoded = RequestSyncPacket.decode(golden.hexToBytes())
        assertNotNull(decoded)
        assertEquals(19, decoded!!.p)
    }

    @Test
    fun goldenVectorRejects() {
        listOf(
            "010001000200040000040003000100",  // p = 0
            "010001210200040000040003000100",  // p = 33 > MAX_P
            "010001010200040000000003000100",  // m = 0
            "0100011302000400080000",          // missing data TLV
            "010001",                          // truncated TLV
        ).forEach { hex ->
            assertNull("expected reject for $hex", RequestSyncPacket.decode(hex.hexToBytes()))
        }
        assertNull(RequestSyncPacket.decode(ByteArray(0)))
    }

    private fun ByteArray.toHex(): String = joinToString("") { "%02x".format(it) }

    private fun String.hexToBytes(): ByteArray =
        chunked(2).map { it.toInt(16).toByte() }.toByteArray()
}
