package com.bitchat.android.nostr

import android.app.Application
import android.util.Log
import com.bitchat.android.model.BitchatFilePacket
import com.bitchat.android.model.BitchatMessage
import com.bitchat.android.model.DeliveryStatus
import com.bitchat.android.model.NoisePayload
import com.bitchat.android.model.NoisePayloadType
import com.bitchat.android.model.PrivateMessagePacket
import com.bitchat.android.protocol.BitchatPacket
import com.bitchat.android.service.MeshServiceHolder
import com.bitchat.android.services.SeenMessageStore
import com.bitchat.android.ui.ChatState
import com.bitchat.android.ui.MeshDelegateHandler
import com.bitchat.android.ui.PrivateChatManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.Date
import java.util.UUID

class NostrDirectMessageHandler(
    private val application: Application,
    private val state: ChatState,
    private val privateChatManager: PrivateChatManager,
    private val meshDelegateHandler: MeshDelegateHandler,
    private val scope: CoroutineScope,
    private val repo: GeohashRepository,
    private val dataManager: com.bitchat.android.ui.DataManager,
    private val notificationManager: com.bitchat.android.ui.NotificationManager
) {
    companion object { private const val TAG = "NostrDirectMessageHandler" }

    private val seenStore by lazy { SeenMessageStore.getInstance(application) }

    // Simple event deduplication
    private val processedIds = ArrayDeque<String>()
    private val seen = HashSet<String>()
    private val max = 2000

    private fun dedupe(id: String): Boolean {
        if (seen.contains(id)) return true
        seen.add(id)
        processedIds.addLast(id)
        if (processedIds.size > max) {
            val old = processedIds.removeFirst()
            seen.remove(old)
        }
        return false
    }

    fun onGiftWrap(giftWrap: NostrEvent, geohash: String, identity: NostrIdentity) {
        scope.launch(Dispatchers.Default) {
            try {
                if (dedupe(giftWrap.id)) return@launch

                val messageAge = System.currentTimeMillis() / 1000 - giftWrap.createdAt
                if (messageAge > 173700) return@launch // 48 hours + 15 mins

                val decryptResult = NostrProtocol.decryptPrivateMessage(giftWrap, identity)
                if (decryptResult == null) {
                    Log.w(TAG, "Failed to decrypt Nostr message")
                    return@launch
                }

                val (content, senderPubkey, rumorTimestamp) = decryptResult

                // If sender is blocked for geohash contexts, drop any events from this pubkey
                // Applies to both geohash DMs (geohash != "") and account DMs (geohash == "")
                if (dataManager.isGeohashUserBlocked(senderPubkey)) return@launch
                if (!content.startsWith("bitchat1:")) return@launch

                val base64Content = content.removePrefix("bitchat1:")
                val packetData = base64URLDecode(base64Content) ?: return@launch
                val packet = BitchatPacket.fromBinaryData(packetData) ?: return@launch

                if (packet.type != com.bitchat.android.protocol.MessageType.NOISE_ENCRYPTED.value) return@launch

                val noisePayload = NoisePayload.decode(packet.payload) ?: return@launch
                val messageTimestamp = Date(giftWrap.createdAt * 1000L)

                // Resolve convKey: use SHORT 16-hex peerID (SHA256-based) for known contacts
                val matchingFav = try {
                    com.bitchat.android.favorites.FavoritesPersistenceService.shared
                        .findRelationshipByNostrPubkey(senderPubkey)
                } catch (_: Exception) { null }
                val (convKey, favSenderNickname) = if (matchingFav != null) {
                    val digest = java.security.MessageDigest.getInstance("SHA-256")
                    val hash = digest.digest(matchingFav.peerNoisePublicKey)
                    val shortPeerID = hash.joinToString("") { "%02x".format(it) }.take(16)
                    shortPeerID to matchingFav.peerNickname.ifBlank { null }
                } else {
                    "nostr_${senderPubkey.take(16)}" to null
                }

                repo.putNostrKeyMapping(convKey, senderPubkey)
                com.bitchat.android.nostr.GeohashAliasRegistry.put(convKey, senderPubkey)
                if (geohash.isNotEmpty()) {
                    // Remember which geohash this conversation belongs to so we can subscribe on-demand
                    repo.setConversationGeohash(convKey, geohash)
                    GeohashConversationRegistry.set(convKey, geohash)
                }

                // Ensure sender appears in geohash people list even if they haven't posted publicly yet
                if (geohash.isNotEmpty()) {
                    // Cache a best-effort nickname and mark as participant
                    val cached = repo.getCachedNickname(senderPubkey)
                    if (cached == null) {
                        val base = repo.displayNameForNostrPubkeyUI(senderPubkey).substringBefore("#")
                        repo.cacheNickname(senderPubkey, base)
                    }
                    repo.updateParticipant(geohash, senderPubkey, messageTimestamp)
                }

                val senderNickname = favSenderNickname ?: repo.displayNameForNostrPubkeyUI(senderPubkey)

                processNoisePayload(noisePayload, convKey, senderNickname, messageTimestamp, senderPubkey, identity)

            } catch (e: Exception) {
                Log.e(TAG, "onGiftWrap error: ${e.message}")
            }
        }
    }

    private suspend fun processNoisePayload(
        payload: NoisePayload,
        convKey: String,
        senderNickname: String,
        timestamp: Date,
        senderPubkey: String,
        recipientIdentity: NostrIdentity
    ) {
        when (payload.type) {
            NoisePayloadType.PRIVATE_MESSAGE -> {
                val pm = PrivateMessagePacket.decode(payload.data) ?: return
                if (pm.content.startsWith("[FAVORITED]") || pm.content.startsWith("[UNFAVORITED]")) {
                    handleFavoriteNotification(pm.content, convKey, senderNickname, senderPubkey)
                    return
                }
                val resolvedRelayPeerID = resolveRelayPeerID(senderPubkey, convKey)
                if (pm.content.startsWith("[profile:") && pm.content.endsWith("]")) {
                    val base64 = pm.content.removePrefix("[profile:").removeSuffix("]")
                    com.bitchat.android.services.ProfileImageService.handleReceivedProfileImage(base64, resolvedRelayPeerID)
                    return
                }
                if (pm.content.startsWith("[grp:")) {
                    val groupSenderPeerID = resolveGroupRelaySenderPeerID(pm.content, senderPubkey, resolvedRelayPeerID)
                    val groupMessage = BitchatMessage(
                        id = pm.messageID,
                        sender = senderNickname,
                        content = pm.content,
                        timestamp = timestamp,
                        isRelay = false,
                        isPrivate = true,
                        recipientNickname = state.getNicknameValue(),
                        senderPeerID = groupSenderPeerID,
                        deliveryStatus = DeliveryStatus.Delivered(to = state.getNicknameValue() ?: "Unknown", at = Date())
                    )
                    withContext(Dispatchers.Main) {
                        com.bitchat.android.ui.groups.GroupChatManager.shared
                            ?.interceptIncomingMessage(groupMessage, groupSenderPeerID)
                    }
                    return
                }
                val existingMessages = state.getPrivateChatsValue()[convKey] ?: emptyList()
                if (existingMessages.any { it.id == pm.messageID }) return

                val message = BitchatMessage(
                    id = pm.messageID,
                    sender = senderNickname,
                    content = pm.content,
                    timestamp = timestamp,
                    isRelay = false,
                    isPrivate = true,
                    recipientNickname = state.getNicknameValue(),
                    senderPeerID = convKey,
                    deliveryStatus = DeliveryStatus.Delivered(to = state.getNicknameValue() ?: "Unknown", at = Date())
                )

                val isViewing = state.getSelectedPrivateChatPeerValue() == convKey
                val suppressUnread = seenStore.hasRead(pm.messageID)

                withContext(Dispatchers.Main) {
                    privateChatManager.handleIncomingPrivateMessage(
                        message,
                        suppressUnread = suppressUnread,
                        forceStore = true
                    )
                }

                if (!seenStore.hasDelivered(pm.messageID)) {
                    val nostrTransport = NostrTransport.getInstance(application)
                    nostrTransport.sendDeliveryAckGeohash(pm.messageID, senderPubkey, recipientIdentity)
                    seenStore.markDelivered(pm.messageID)
                }

                if (isViewing && !suppressUnread) {
                    val nostrTransport = NostrTransport.getInstance(application)
                    nostrTransport.sendReadReceiptGeohash(pm.messageID, senderPubkey, recipientIdentity)
                    seenStore.markRead(pm.messageID)
                }
            }
            NoisePayloadType.DELIVERED -> {
                val messageId = String(payload.data, Charsets.UTF_8)
                withContext(Dispatchers.Main) {
                    meshDelegateHandler.didReceiveDeliveryAck(messageId, convKey)
                }
            }
            NoisePayloadType.READ_RECEIPT -> {
                val messageId = String(payload.data, Charsets.UTF_8)
                withContext(Dispatchers.Main) {
                    meshDelegateHandler.didReceiveReadReceipt(messageId, convKey)
                }
            }
            NoisePayloadType.FILE_TRANSFER -> {
                // Properly handle encrypted file transfer
                val file = BitchatFilePacket.decode(payload.data)
                if (file != null) {
                    val uniqueMsgId = java.util.UUID.randomUUID().toString().uppercase()
                    val savedPath = com.bitchat.android.features.file.FileUtils.saveIncomingFile(application, file)
                    val message = BitchatMessage(
                        id = uniqueMsgId,
                        sender = senderNickname,
                        content = savedPath,
                        type = com.bitchat.android.features.file.FileUtils.messageTypeForMime(file.mimeType),
                        timestamp = timestamp,
                        isRelay = false,
                        isPrivate = true,
                        recipientNickname = state.getNicknameValue(),
                        senderPeerID = convKey
                    )
                    Log.d(TAG, "📄 Saved Nostr encrypted incoming file to $savedPath (msgId=$uniqueMsgId)")
                    withContext(Dispatchers.Main) {
                        privateChatManager.handleIncomingPrivateMessage(
                            message,
                            suppressUnread = false,
                            forceStore = true
                        )
                    }
                } else {
                    Log.w(TAG, "⚠️ Failed to decode Nostr file transfer from $convKey")
                }
            }
            NoisePayloadType.VERIFY_CHALLENGE,
            NoisePayloadType.VERIFY_RESPONSE -> Unit // Ignore verification payloads in Nostr direct messages
        }
    }

    private fun base64URLDecode(input: String): ByteArray? {
        return try {
            val padded = input.replace("-", "+")
                .replace("_", "/")
                .let { str ->
                    val padding = (4 - str.length % 4) % 4
                    str + "=".repeat(padding)
                }
            android.util.Base64.decode(padded, android.util.Base64.DEFAULT)
        } catch (e: Exception) {
            Log.e(TAG, "Failed to decode base64url: ${e.message}")
            null
        }
    }

    private suspend fun handleFavoriteNotification(
        content: String,
        convKey: String,
        senderNickname: String,
        senderPubkey: String
    ) {
        val isFavorite = content.startsWith("[FAVORITED]") || content.contains("FAVORITE:TRUE")
        val senderKey = favoriteStorageKeyForNostrPubkey(senderPubkey)
        val previous = com.bitchat.android.favorites.FavoritesPersistenceService.shared
            .getFavoriteStatus(senderKey)
            ?.theyFavoritedUs == true
        val normalizedNpub = extractFavoriteNostrPubkey(content) ?: normalizedNostrPublicKey(senderPubkey)

        com.bitchat.android.favorites.FavoritesPersistenceService.shared.updatePeerFavoritedUs(
            noisePublicKey = senderKey,
            theyFavoritedUs = isFavorite,
            peerNickname = senderNickname,
            peerNostrPublicKey = normalizedNpub
        )

        notificationManager.showFavoriteStatusNotification(
            title = if (isFavorite) "New Favorite" else "Favorite Removed",
            body = "$senderNickname ${if (isFavorite) "favorited" else "unfavorited"} you",
            peerID = convKey
        )

        normalizedNpub?.let {
            runCatching {
                com.bitchat.android.favorites.FavoritesPersistenceService.shared
                    .findPeerIDForNostrPubkey(it)
                    ?.let { peerID ->
                        com.bitchat.android.favorites.FavoritesPersistenceService.shared
                            .updateNostrPublicKeyForPeerID(peerID, it)
                    }
            }
        }

        if (previous != isFavorite) {
            val action = if (isFavorite) "favorited" else "unfavorited"
            withContext(Dispatchers.Main) {
                privateChatManager.handleIncomingPrivateMessage(
                    BitchatMessage(
                        id = "fav-${UUID.randomUUID()}",
                        sender = "system",
                        content = "$senderNickname $action you",
                        timestamp = Date(),
                        isRelay = false,
                        isPrivate = true,
                        senderPeerID = convKey
                    ),
                    suppressUnread = true,
                    forceStore = true
                )
            }
        }

        refreshProfileSharingAccess(convKey, senderNickname)
    }

    private fun refreshProfileSharingAccess(peerID: String, nickname: String) {
        val meshService = MeshServiceHolder.meshService ?: return
        if (com.bitchat.android.services.ProfileImageService.canExchangeProfile(peerID)) {
            val envelope = com.bitchat.android.services.ProfileImageService.encodeMyProfileForSync() ?: return
            if (!com.bitchat.android.services.ProfileImageService.shouldSendProfile(peerID)) return
            val messageID = UUID.randomUUID().toString()
            com.bitchat.android.services.MessageRouter
                .getInstance(application, meshService)
                .sendPrivate(envelope, peerID, nickname, messageID)
            com.bitchat.android.services.ProfileImageService.markProfileSent(peerID)
        } else {
            com.bitchat.android.services.ProfileImageService.removePeerProfileImage(peerID)
        }
    }

    private fun extractFavoriteNostrPubkey(content: String): String? {
        val value = content.substringAfter(':', "").trim()
        return value.takeIf { it.isNotEmpty() }
    }

    private fun normalizedNostrPublicKey(value: String): String? {
        return try {
            if (value.startsWith("npub1")) {
                value
            } else {
                Bech32.encode("npub", value.chunked(2).map { it.toInt(16).toByte() }.toByteArray())
            }
        } catch (_: Exception) {
            null
        }
    }

    private fun resolveRelayPeerID(senderPubkey: String, fallback: String): String {
        return com.bitchat.android.favorites.FavoritesPersistenceService.shared
            .findPeerIDForNostrPubkey(senderPubkey)
            ?: fallback
    }

    private fun resolveGroupRelaySenderPeerID(content: String, senderPubkey: String, fallback: String): String {
        val envelope = com.bitchat.android.ui.groups.GroupEnvelope.parse(content) ?: return fallback
        val groupID = when (envelope) {
            is com.bitchat.android.ui.groups.GroupEnvelope.Message -> envelope.groupID
            is com.bitchat.android.ui.groups.GroupEnvelope.Invite -> envelope.groupID
            is com.bitchat.android.ui.groups.GroupEnvelope.Accept -> envelope.groupID
            is com.bitchat.android.ui.groups.GroupEnvelope.Leave -> envelope.groupID
            is com.bitchat.android.ui.groups.GroupEnvelope.Sync -> envelope.groupID
            is com.bitchat.android.ui.groups.GroupEnvelope.SyncReply -> envelope.groupID
            is com.bitchat.android.ui.groups.GroupEnvelope.Meta -> envelope.groupID
            is com.bitchat.android.ui.groups.GroupEnvelope.Role -> envelope.groupID
            is com.bitchat.android.ui.groups.GroupEnvelope.Pin -> envelope.groupID
            is com.bitchat.android.ui.groups.GroupEnvelope.Unpin -> envelope.groupID
            is com.bitchat.android.ui.groups.GroupEnvelope.Poll -> envelope.groupID
            is com.bitchat.android.ui.groups.GroupEnvelope.Vote -> envelope.groupID
            is com.bitchat.android.ui.groups.GroupEnvelope.JoinRequest -> envelope.groupID
            is com.bitchat.android.ui.groups.GroupEnvelope.JoinAccept -> envelope.groupID
        }

        com.bitchat.android.ui.groups.GroupChatManager.shared
            ?.canonicalPeerIDForNostrHint(groupID, senderPubkey)
            ?.let { return it }

        return when (envelope) {
            is com.bitchat.android.ui.groups.GroupEnvelope.Invite ->
                envelope.payload.members.firstOrNull { memberMatchesNostrHint(it.nostrPublicKey, senderPubkey) }?.peerID ?: fallback
            is com.bitchat.android.ui.groups.GroupEnvelope.JoinAccept ->
                envelope.payload.members.firstOrNull { memberMatchesNostrHint(it.nostrPublicKey, senderPubkey) }?.peerID ?: fallback
            else -> fallback
        }
    }

    private fun memberMatchesNostrHint(memberNostrKey: String?, senderHint: String): Boolean {
        val memberHex = normalizedNostrHex(memberNostrKey) ?: return false
        val senderHex = normalizedNostrHex(senderHint) ?: return false
        return memberHex == senderHex || memberHex.startsWith(senderHex) || senderHex.startsWith(memberHex)
    }

    private fun normalizedNostrHex(value: String?): String? {
        val trimmed = value?.trim()?.lowercase().orEmpty()
        if (trimmed.isEmpty()) return null
        return try {
            when {
                trimmed.startsWith("npub1") -> {
                    val (hrp, data) = Bech32.decode(trimmed)
                    if (hrp == "npub") data.joinToString("") { "%02x".format(it) } else null
                }
                trimmed.all { it in '0'..'9' || it in 'a'..'f' } -> trimmed
                else -> null
            }
        } catch (_: Exception) {
            null
        }
    }

    private fun favoriteStorageKeyForNostrPubkey(nostrPubkey: String): ByteArray {
        com.bitchat.android.favorites.FavoritesPersistenceService.shared.findNoiseKey(nostrPubkey)?.let { return it }
        return try {
            if (nostrPubkey.startsWith("npub1")) {
                val (hrp, data) = Bech32.decode(nostrPubkey)
                if (hrp == "npub") data else nostrPubkey.toByteArray()
            } else {
                nostrPubkey.chunked(2).map { it.toInt(16).toByte() }.toByteArray()
            }
        } catch (_: Exception) {
            nostrPubkey.toByteArray()
        }
    }
}
