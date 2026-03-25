// SDK stub — host app provides the real implementation
package com.bitchat.android.ui

import com.bitchat.android.model.BitchatMessage
import kotlinx.coroutines.CoroutineScope

/**
 * Minimal stub of ChatState exposing only the API surface used by the nostr module.
 * The host app provides the real ChatState with full StateFlow-backed observable state.
 */
open class ChatState(scope: CoroutineScope) {

    // --- Getters used by nostr module ---

    open fun getNicknameValue(): String? = null

    open fun getPrivateChatsValue(): Map<String, List<BitchatMessage>> = emptyMap()

    open fun getSelectedPrivateChatPeerValue(): String? = null

    open fun getTeleportedGeoValue(): Set<String> = emptySet()

    // --- Setters used by nostr module ---

    open fun setGeohashPeople(people: List<GeoPerson>) {}

    open fun setTeleportedGeo(teleported: Set<String>) {}

    open fun postTeleportedGeo(teleported: Set<String>) {}

    open fun setGeohashParticipantCounts(counts: Map<String, Int>) {}
}
