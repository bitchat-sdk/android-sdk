// SDK stub — host app provides the real implementation
package com.bitchat.android.ui

import com.bitchat.android.model.BitchatMessage

/**
 * Minimal stub of MessageManager exposing only the method used by the nostr module.
 * The host app provides the real MessageManager with full message lifecycle management.
 */
open class MessageManager(private val state: ChatState) {

    /**
     * Add a message to a specific channel's message list.
     * Called by GeohashMessageHandler to emit geohash chat messages.
     */
    open fun addChannelMessage(channel: String, message: BitchatMessage) {}
}
