// SDK stub — host app provides the real implementation
package com.bitchat.android.geohash

/**
 * Minimal stub of Geohash exposing only the methods used by the nostr module.
 * The host app provides the real Geohash with full encode/decode/neighbor logic.
 */
object Geohash {

    /**
     * Decode a geohash string to its center latitude/longitude pair.
     */
    fun decodeToCenter(geohash: String): Pair<Double, Double> = Pair(0.0, 0.0)

    /**
     * Compute the set of neighboring geohashes at the same precision level.
     */
    fun neighborsSamePrecision(geohash: String): Set<String> = emptySet()
}
