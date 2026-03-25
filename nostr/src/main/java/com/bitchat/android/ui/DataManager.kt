// SDK stub — host app provides the real implementation
package com.bitchat.android.ui

/**
 * Minimal stub of DataManager exposing only the method used by the nostr module.
 * The host app provides the real DataManager backed by SharedPreferences.
 */
open class DataManager {

    /**
     * Check whether a Nostr pubkey (hex) is blocked in geohash contexts.
     */
    open fun isGeohashUserBlocked(pubkeyHex: String): Boolean = false
}
