# bitchat-android-sdk

[![Android API 26+](https://img.shields.io/badge/Android-API%2026%2B-green)](https://developer.android.com)
[![Kotlin](https://img.shields.io/badge/Kotlin-2.x-purple)](https://kotlinlang.org)
[![License: Unlicense](https://img.shields.io/badge/license-Unlicense-blue.svg)](https://unlicense.org)

Android SDK — BitChat binary protocol and Nostr transport for Android.

Two Gradle modules, published separately to Maven Central:

| Module | Maven Coordinates | Description |
|--------|-------------------|-------------|
| `protocol-core` | `io.github.bitchat-sdk:protocol-core:0.1.0` | Binary packet encode/decode, TLV codec, peer ID utilities |
| `nostr` | `io.github.bitchat-sdk:nostr:0.1.0` | Nostr relay client, NIP-17 gift wrap, geohash relay discovery |

## Installation

```kotlin
// build.gradle.kts (app or library)
dependencies {
    implementation("io.github.bitchat-sdk:protocol-core:0.1.0")
    implementation("io.github.bitchat-sdk:nostr:0.1.0") // depends on protocol-core
}
```

## Quick Start

### Encode a BitChat packet

```kotlin
import io.github.bitchat-sdk.protocol.BinaryProtocol
import io.github.bitchat-sdk.protocol.BitchatPacket

val packet = BitchatPacket(
    version = 1,
    type = MessageType.MESSAGE.rawValue,
    ttl = 7,
    timestamp = System.currentTimeMillis(),
    flags = 0,
    senderID = myPeerID,
    payload = "Hello, BitChat!".toByteArray()
)
val wire: ByteArray = BinaryProtocol.encode(packet)

// Decode
val decoded: BitchatPacket? = BinaryProtocol.decode(wire)
```

### Connect to a Nostr relay

```kotlin
import io.github.bitchat-sdk.nostr.NostrClient

val client = NostrClient(relayUrl = "wss://relay.damus.io")
client.connect()
client.subscribe(filters = listOf(NostrFilter(kinds = listOf(1059)))) { event ->
    val embedded = NostrEmbeddedBitChat.extractPacket(event)
    // handle embedded BitChat packet
}
```

## Requirements

- Android API 26+
- Kotlin 2.x

## Publishing

CI publishes to Maven Central on version tags (`v*`).

Required secrets: `OSSRH_USERNAME`, `OSSRH_PASSWORD`, `SIGNING_KEY`, `SIGNING_PASSWORD`.

```bash
./gradlew :protocol-core:publish
./gradlew :nostr:publish
```

## License

Unlicense — public domain.
