// SDK stub — host app provides the real implementation
package com.bitchat.android.ui

import com.bitchat.android.model.BitchatMessage

/**
 * Minimal stub of PrivateChatManager exposing only the method used by the nostr module.
 * The host app provides the real PrivateChatManager with full private chat management.
 */
open class PrivateChatManager {

    /**
     * Handle an incoming private message from any transport (mesh, Nostr, etc.).
     *
     * @param message        The incoming message.
     * @param suppressUnread If true, do not mark the conversation as unread.
     * @param forceStore     If true, always persist the message into the UI state.
     */
    open fun handleIncomingPrivateMessage(
        message: BitchatMessage,
        suppressUnread: Boolean = false,
        forceStore: Boolean = false
    ) {}
}
