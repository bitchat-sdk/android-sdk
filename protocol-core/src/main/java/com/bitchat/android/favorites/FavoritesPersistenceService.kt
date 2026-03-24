package com.bitchat.android.favorites

/**
 * Status of a favorite relationship with a peer.
 */
data class FavoriteStatus(
    val theyFavoritedUs: Boolean = false,
    val peerNostrPublicKey: String? = null,
    val peerNickname: String = ""
)

/**
 * Relationship record returned when looking up a peer by Nostr public key.
 */
data class FavoriteRelationship(
    val peerNoisePublicKey: ByteArray,
    val peerNickname: String = "",
    val peerNostrPublicKey: String? = null
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is FavoriteRelationship) return false
        return peerNoisePublicKey.contentEquals(other.peerNoisePublicKey)
    }

    override fun hashCode(): Int = peerNoisePublicKey.contentHashCode()
}

/**
 * Persistence service for favorite (trusted-contact) relationships.
 *
 * This is a minimal stub that satisfies compile-time references from the
 * `nostr` module.  Host applications must provide a real implementation
 * by assigning to [shared] at startup, typically backed by a database
 * or encrypted preferences.
 */
open class FavoritesPersistenceService {

    companion object {
        /** Application-wide singleton. Replace with a real implementation at app startup. */
        @Volatile
        var shared: FavoritesPersistenceService = FavoritesPersistenceService()
    }

    // -- Query methods --

    /** Find the BitChat peer ID associated with a Nostr public key (npub or hex). */
    open fun findPeerIDForNostrPubkey(nostrPubkey: String): String? = null

    /** Find the Nostr public key (npub) associated with a BitChat peer ID. */
    open fun findNostrPubkeyForPeerID(peerID: String): String? = null

    /** Look up favorite status by raw Noise public key bytes. */
    open fun getFavoriteStatus(noisePublicKey: ByteArray): FavoriteStatus? = null

    /** Look up favorite status by peer ID string (short 16-hex form). */
    open fun getFavoriteStatus(peerID: String): FavoriteStatus? = null

    /** Find a full relationship record by Nostr public key. */
    open fun findRelationshipByNostrPubkey(nostrPubkey: String): FavoriteRelationship? = null

    /** Reverse-lookup: find the raw Noise public key for a Nostr public key. */
    open fun findNoiseKey(nostrPubkey: String): ByteArray? = null

    // -- Mutation methods --

    /** Record whether a remote peer has favorited us. */
    open fun updatePeerFavoritedUs(
        noisePublicKey: ByteArray,
        theyFavoritedUs: Boolean,
        peerNickname: String,
        peerNostrPublicKey: String?
    ) { /* no-op stub */ }

    /** Associate a Nostr public key with an existing peer ID record. */
    open fun updateNostrPublicKeyForPeerID(peerID: String, nostrPubkey: String) { /* no-op stub */ }
}
