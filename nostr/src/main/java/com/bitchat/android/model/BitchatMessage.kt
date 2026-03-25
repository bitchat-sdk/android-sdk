// SDK stub — host app provides the real implementation
package com.bitchat.android.model

import java.util.Date
import java.util.UUID

/**
 * Minimal stub for BitchatMessage used by the nostr module.
 * The host app replaces this with its full Parcelable implementation.
 */
data class BitchatMessage(
    val id: String = UUID.randomUUID().toString().uppercase(),
    val sender: String,
    val content: String,
    val type: BitchatMessageType = BitchatMessageType.Message,
    val timestamp: Date,
    val isRelay: Boolean = false,
    val originalSender: String? = null,
    val isPrivate: Boolean = false,
    val recipientNickname: String? = null,
    val senderPeerID: String? = null,
    val mentions: List<String>? = null,
    val channel: String? = null,
    val deliveryStatus: DeliveryStatus? = null,
    val powDifficulty: Int? = null
)

enum class BitchatMessageType {
    Message,
    Audio,
    Image,
    File
}

sealed class DeliveryStatus {
    object Sending : DeliveryStatus()
    object Sent : DeliveryStatus()
    data class Delivered(val to: String, val at: Date) : DeliveryStatus()
    data class Read(val by: String, val at: Date) : DeliveryStatus()
    data class Failed(val reason: String) : DeliveryStatus()
    data class PartiallyDelivered(val reached: Int, val total: Int) : DeliveryStatus()
}
