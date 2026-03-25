// SDK stub — host app provides the real implementation
package com.bitchat.android.services

/**
 * Minimal stub of ProfileImageService exposing only the methods used by the nostr module.
 * The host app provides the real ProfileImageService with full profile image exchange logic.
 */
object ProfileImageService {

    fun handleReceivedProfileImage(base64: String, fromPeerID: String) {}

    fun canExchangeProfile(peerID: String): Boolean = false

    fun encodeMyProfileForSync(): String? = null

    fun shouldSendProfile(toPeerID: String): Boolean = false

    fun markProfileSent(toPeerID: String) {}

    fun removePeerProfileImage(peerID: String) {}
}
