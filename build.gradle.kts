// Top-level build file — configuration for all subprojects/modules.
plugins {
    alias(libs.plugins.android.library) apply false
    alias(libs.plugins.kotlin.android) apply false
    alias(libs.plugins.kotlin.parcelize) apply false
}

// Shared publishing coordinates — override per-module if needed
ext {
    set("GROUP_ID", "io.github.bitchat-sdk")
    set("VERSION", "0.1.0")
    set("POM_URL", "https://github.com/bitchat-sdk/android-sdk")
    set("POM_SCM_URL", "https://github.com/bitchat-sdk/android-sdk")
    set("POM_SCM_CONNECTION", "scm:git:github.com/bitchat-sdk/android-sdk.git")
    set("POM_SCM_DEV_CONNECTION", "scm:git:ssh://github.com/bitchat-sdk/android-sdk.git")
    set("POM_LICENCE_NAME", "The Unlicense")
    set("POM_LICENCE_URL", "https://unlicense.org")
    set("POM_DEVELOPER_ID", "bitchat-sdk")
    set("POM_DEVELOPER_NAME", "bitchat-sdk")
}
