// SDK stub — host app provides the real implementation
package com.bitchat.android.services

import android.content.Context

/**
 * Minimal stub of SeenMessageStore exposing only the methods used by the nostr module.
 * The host app provides the real SeenMessageStore backed by encrypted shared preferences.
 */
open class SeenMessageStore private constructor(context: Context) {

    companion object {
        @Volatile
        private var INSTANCE: SeenMessageStore? = null

        fun getInstance(appContext: Context): SeenMessageStore {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: SeenMessageStore(appContext.applicationContext).also { INSTANCE = it }
            }
        }
    }

    open fun hasDelivered(id: String): Boolean = false

    open fun hasRead(id: String): Boolean = false

    open fun markDelivered(id: String) {}

    open fun markRead(id: String) {}
}
