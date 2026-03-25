// SDK stub — host app provides the real implementation
package com.bitchat.android.geohash

import android.content.Context
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

/**
 * Minimal stub of LocationChannelManager exposing only the API used by the nostr module.
 * The host app provides the real LocationChannelManager with location tracking and channel selection.
 */
open class LocationChannelManager private constructor(context: Context) {

    companion object {
        @Volatile
        private var INSTANCE: LocationChannelManager? = null

        fun getInstance(context: Context): LocationChannelManager {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: LocationChannelManager(context.applicationContext).also { INSTANCE = it }
            }
        }
    }

    private val _selectedChannel = MutableStateFlow<ChannelID>(ChannelID.Mesh)

    /** The currently selected channel (Mesh or a Location geohash). */
    val selectedChannel: StateFlow<ChannelID> = _selectedChannel
}
