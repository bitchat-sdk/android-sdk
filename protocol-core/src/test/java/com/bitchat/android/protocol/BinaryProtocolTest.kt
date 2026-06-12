package com.bitchat.android.protocol

import org.junit.Assert.assertArrayEquals
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Test

class BinaryProtocolTest {

    private fun makePacket(version: UByte = 1u) = BitchatPacket(
        version = version,
        type = 0x01u,
        senderID = ByteArray(8) { 0xAA.toByte() },
        recipientID = null,
        timestamp = 1234567890uL,
        payload = byteArrayOf(0x10, 0x20, 0x30),
        signature = null,
        ttl = 3u
    )

    @Test
    fun roundTripV1() {
        val encoded = BinaryProtocol.encode(makePacket(version = 1u))
        assertNotNull(encoded)
        val decoded = BinaryProtocol.decode(encoded!!)
        assertNotNull(decoded)
        assertEquals(1u.toUByte(), decoded!!.version)
        assertArrayEquals(byteArrayOf(0x10, 0x20, 0x30), decoded.payload)
    }

    @Test
    fun roundTripV2() {
        val encoded = BinaryProtocol.encode(makePacket(version = 2u))
        assertNotNull(encoded)
        val decoded = BinaryProtocol.decode(encoded!!)
        assertNotNull(decoded)
        assertEquals(2u.toUByte(), decoded!!.version)
        assertArrayEquals(byteArrayOf(0x10, 0x20, 0x30), decoded.payload)
    }

    @Test
    fun decodeRejectsInputShorterThanV1HeaderPlusSender() {
        // v1 header (14 bytes) + senderID (8 bytes) = 22-byte minimum frame
        val truncated = ByteArray(21).also { it[0] = 1 }
        assertNull(BinaryProtocol.decode(truncated))
    }

    @Test
    fun decodeRejectsEmptyAndGarbage() {
        assertNull(BinaryProtocol.decode(ByteArray(0)))
        assertNull(BinaryProtocol.decode(byteArrayOf(0xFF.toByte(), 0xFF.toByte(), 0xFF.toByte())))
    }
}
