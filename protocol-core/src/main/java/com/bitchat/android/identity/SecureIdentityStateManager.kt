package com.bitchat.android.identity

import android.content.Context

/**
 * Secure key-value storage backed by Android EncryptedSharedPreferences.
 *
 * Provides a simple API for persisting sensitive identity material
 * (Nostr private keys, device seeds, etc.) in encrypted form.
 *
 * Host applications must supply their own implementation by subclassing
 * or by replacing this stub with the real encrypted-preferences wrapper.
 */
open class SecureIdentityStateManager(private val context: Context) {

    private val prefs by lazy {
        context.getSharedPreferences("secure_identity_state", Context.MODE_PRIVATE)
    }

    /** Retrieve a previously stored value, or `null` if absent. */
    open fun getSecureValue(key: String): String? {
        return prefs.getString(key, null)
    }

    /** Persist a value under the given key. */
    open fun storeSecureValue(key: String, value: String) {
        prefs.edit().putString(key, value).apply()
    }

    /** Remove one or more keys from storage. */
    open fun clearSecureValues(vararg keys: String) {
        val editor = prefs.edit()
        for (key in keys) {
            editor.remove(key)
        }
        editor.apply()
    }
}
