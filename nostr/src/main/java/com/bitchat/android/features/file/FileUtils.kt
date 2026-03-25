// SDK stub — host app provides the real implementation
package com.bitchat.android.features.file

import android.content.Context
import com.bitchat.android.model.BitchatFilePacket
import com.bitchat.android.model.BitchatMessageType

/**
 * Minimal stub of FileUtils exposing only the methods used by the nostr module.
 * The host app provides the real FileUtils with full file I/O and MIME handling.
 */
object FileUtils {

    /**
     * Save an incoming file packet to local storage and return the file path.
     */
    fun saveIncomingFile(context: Context, file: BitchatFilePacket): String = ""

    /**
     * Map a MIME type string to the corresponding BitchatMessageType.
     */
    fun messageTypeForMime(mime: String): BitchatMessageType = BitchatMessageType.File
}
