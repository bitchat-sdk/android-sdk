// SDK stub — host app provides the real implementation
package com.bitchat.android.services

import android.content.Context

/**
 * Minimal stub of MessageRouter exposing only the methods used by the nostr module.
 * The host app provides the real MessageRouter with full mesh + Nostr routing.
 */
open class MessageRouter private constructor() {

    companion object {
        @Volatile
        private var INSTANCE: MessageRouter? = null

        fun tryGetInstance(): MessageRouter? = INSTANCE

        fun getInstance(context: Context, mesh: Any): MessageRouter {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: MessageRouter().also { INSTANCE = it }
            }
        }
    }

    /**
     * Send a private message to a peer, routing via mesh or Nostr as appropriate.
     */
    open fun sendPrivate(content: String, toPeerID: String, recipientNickname: String, messageID: String) {}
}
