// SDK stub — host app provides the real implementation
package com.bitchat.android.ui

/**
 * Minimal stub of MeshDelegateHandler exposing only the methods used by the nostr module.
 * The host app provides the real MeshDelegateHandler that implements BluetoothMeshDelegate.
 */
open class MeshDelegateHandler {

    /**
     * Called when a delivery acknowledgement is received for a message.
     */
    open fun didReceiveDeliveryAck(messageID: String, recipientPeerID: String) {}

    /**
     * Called when a read receipt is received for a message.
     */
    open fun didReceiveReadReceipt(messageID: String, recipientPeerID: String) {}
}
