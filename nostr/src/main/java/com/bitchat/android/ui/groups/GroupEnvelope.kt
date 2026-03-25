// SDK stub — host app provides the real implementation
package com.bitchat.android.ui.groups

/**
 * Minimal stub of GroupEnvelope exposing only the variants referenced by the nostr module.
 * The host app provides the real GroupEnvelope with full parsing logic.
 */
sealed class GroupEnvelope {
    data class Message(val groupID: String, val vectorClock: Long, val content: String) : GroupEnvelope()
    data class Invite(val groupID: String, val payload: GroupInvitePayload) : GroupEnvelope()
    data class Accept(val groupID: String) : GroupEnvelope()
    data class Leave(val groupID: String) : GroupEnvelope()
    data class Sync(val groupID: String, val sinceVectorClock: Long) : GroupEnvelope()
    data class SyncReply(val groupID: String, val payload: GroupSyncReplyPayload) : GroupEnvelope()
    data class Meta(val groupID: String, val payload: GroupMetadataUpdate) : GroupEnvelope()
    data class Role(val groupID: String, val payload: GroupRoleChangePayload) : GroupEnvelope()
    data class Pin(val groupID: String, val payload: GroupPinPayload) : GroupEnvelope()
    data class Unpin(val groupID: String, val payload: GroupPinPayload) : GroupEnvelope()
    data class Poll(val groupID: String, val payload: GroupPollPayload) : GroupEnvelope()
    data class Vote(val groupID: String, val payload: GroupVotePayload) : GroupEnvelope()
    data class JoinRequest(val groupID: String, val payload: GroupJoinRequestPayload) : GroupEnvelope()
    data class JoinAccept(val groupID: String, val payload: GroupJoinAcceptPayload) : GroupEnvelope()

    companion object {
        fun parse(content: String): GroupEnvelope? = null
    }
}

// Minimal payload stubs referenced by GroupEnvelope variants

data class GroupInvitePayload(
    val members: List<GroupMemberInfo> = emptyList()
)

data class GroupSyncReplyPayload(val placeholder: String = "")

data class GroupMetadataUpdate(val placeholder: String = "")

data class GroupRoleChangePayload(val placeholder: String = "")

data class GroupPinPayload(val placeholder: String = "")

data class GroupPollPayload(val placeholder: String = "")

data class GroupVotePayload(val placeholder: String = "")

data class GroupJoinRequestPayload(val placeholder: String = "")

data class GroupJoinAcceptPayload(
    val members: List<GroupMemberInfo> = emptyList()
)

data class GroupMemberInfo(
    val peerID: String = "",
    val nostrPublicKey: String? = null
)
