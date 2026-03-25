// SDK stub — host app provides the real implementation
package com.bitchat.android.geohash

/**
 * Minimal stubs for ChannelID and supporting types used by the nostr module.
 * The host app provides the real implementations with full geohash channel logic.
 */

data class GeohashChannel(
    val level: GeohashChannelLevel,
    val geohash: String
)

enum class GeohashChannelLevel(val precision: Int, val displayName: String) {
    BUILDING(8, "Building"),
    BLOCK(7, "Block"),
    NEIGHBORHOOD(6, "Neighborhood"),
    CITY(5, "City"),
    PROVINCE(4, "Province"),
    REGION(2, "REGION")
}

sealed class ChannelID {
    object Mesh : ChannelID()
    data class Location(val channel: GeohashChannel) : ChannelID()
}
