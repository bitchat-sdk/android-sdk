// SDK stub — host app provides the real implementation
package com.bitchat.android.net

import okhttp3.OkHttpClient

/**
 * Minimal stub of OkHttpProvider exposing only the methods used by the nostr module.
 * The host app provides the real OkHttpProvider with Tor/proxy support.
 */
object OkHttpProvider {

    private val defaultClient by lazy { OkHttpClient() }

    /**
     * Returns an OkHttpClient suitable for standard HTTP calls.
     */
    fun httpClient(): OkHttpClient = defaultClient

    /**
     * Returns an OkHttpClient configured for WebSocket connections.
     */
    fun webSocketClient(): OkHttpClient = defaultClient
}
