// SDK stub — host app provides the real implementation
package com.bitchat.android.service

/**
 * Minimal stub for MeshServiceHolder.
 * The host app provides the real singleton that holds a reference to BluetoothMeshService.
 */
object MeshServiceHolder {
    /**
     * The current mesh service instance, or null if not yet initialized.
     * The nostr module reads this to resolve mesh transport for profile sharing.
     */
    var meshService: Any? = null
        private set
}
