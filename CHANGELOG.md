# Changelog

All notable changes to the bitchat-android-sdk will be documented here.

The format follows [Keep a Changelog](https://keepachangelog.com/en/1.0.0/).

## [Unreleased]

## [0.1.1] — 2026-04-10

### Security
- Add payload length validation in `BinaryProtocol.decode()` — reject packets exceeding 10 MB (upstream Android 1.7.2, PR #666)
- Add fragment reassembly safety constants: `MAX_FRAGMENTS_PER_ID`, `MAX_FRAGMENT_TOTAL_BYTES`, `MAX_ACTIVE_FRAGMENT_SETS`, `MAX_GLOBAL_FRAGMENT_TOTAL_BYTES`
- Catch `Throwable` instead of `Exception` in packet decoding to prevent OOM/SOE bypass

## [0.1.0] — 2026-03-23

### Added
- `protocol-core`: Binary packet encode/decode (v1/v2 wire format), TLV codec, peer ID utilities
- `nostr`: Nostr relay client (NIP-01), NIP-17 gift-wrap DMs, geohash relay discovery, `NostrEmbeddedBitChat` helpers
- Maven Central publishing via `maven-publish` + `signing` plugins
- Gradle version catalog (`libs.versions.toml`)

[Unreleased]: https://github.com/bitchat-sdk/android-sdk/compare/v0.1.1...HEAD
[0.1.1]: https://github.com/bitchat-sdk/android-sdk/compare/v0.1.0...v0.1.1
[0.1.0]: https://github.com/bitchat-sdk/android-sdk/releases/tag/v0.1.0
