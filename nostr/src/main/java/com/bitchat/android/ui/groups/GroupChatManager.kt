// SDK stub — host app provides the real implementation
package com.bitchat.android.ui.groups

import com.bitchat.android.model.BitchatMessage

/**
 * Minimal stub of GroupChatManager exposing only the methods used by the nostr module.
 * The host app provides the real GroupChatManager with full group chat support.
 */
open class GroupChatManager {

    companion object {
        @Volatile
        var shared: GroupChatManager? = null
    }

    /**
     * Intercept an incoming message that may be a group envelope.
     * Returns true if the message was handled as a group message.
     */
    open fun interceptIncomingMessage(message: BitchatMessage, senderPeerID: String): Boolean = false

    /**
     * Resolve the canonical peerID for a Nostr pubkey hint within a group.
     */
    open fun canonicalPeerIDForNostrHint(groupID: String, senderHint: String): String? = null

    /**
     * Resolve the Nostr public key (npub) for a given peerID across all groups.
     */
    open fun nostrPublicKeyForPeer(peerID: String): String? = null
}
