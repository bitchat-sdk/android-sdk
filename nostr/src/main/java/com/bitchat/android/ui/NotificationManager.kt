// SDK stub — host app provides the real implementation
package com.bitchat.android.ui

/**
 * Minimal stub of NotificationManager exposing only the method used by the nostr module.
 * The host app provides the real NotificationManager with full notification channel support.
 */
open class NotificationManager {

    /**
     * Show a notification when a peer's favorite status changes.
     */
    open fun showFavoriteStatusNotification(title: String, body: String, peerID: String? = null) {}
}
