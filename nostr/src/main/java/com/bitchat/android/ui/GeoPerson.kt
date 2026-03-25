// SDK stub — host app provides the real implementation
package com.bitchat.android.ui

import java.util.Date

/**
 * Minimal stub for GeoPerson used by GeohashRepository.
 * Represents a participant discovered via Nostr ephemeral events.
 */
data class GeoPerson(
    val id: String,
    val displayName: String,
    val lastSeen: Date
)
