import com.vanniktech.maven.publish.AndroidSingleVariantLibrary
import com.vanniktech.maven.publish.SonatypeHost

plugins {
    alias(libs.plugins.android.library)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.parcelize)
    alias(libs.plugins.vanniktech.maven.publish)
}

android {
    namespace = "com.bitchat.android.protocol"
    compileSdk = libs.versions.compileSdk.get().toInt()

    defaultConfig {
        minSdk = libs.versions.minSdk.get().toInt()
        consumerProguardFiles("consumer-rules.pro")
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }
    kotlinOptions {
        jvmTarget = "1.8"
    }

    testOptions {
        unitTests.isReturnDefaultValues = true
    }
}

dependencies {
    implementation(libs.bundles.cryptography)
    implementation(libs.kotlinx.coroutines.android)
    testImplementation(libs.junit)
}

mavenPublishing {
    publishToMavenCentral(SonatypeHost.CENTRAL_PORTAL, automaticRelease = true)
    signAllPublications()
    configure(AndroidSingleVariantLibrary(variant = "release", sourcesJar = true, publishJavadocJar = true))
    coordinates(
        rootProject.ext.get("GROUP_ID").toString(),
        "protocol-core",
        rootProject.ext.get("VERSION").toString()
    )

    pom {
        name.set("bitchat-android-protocol-core")
        description.set("BitChat binary protocol encode/decode for Android — packet structs, TLV codec, peer ID utilities")
        url.set(rootProject.ext.get("POM_URL").toString())
        licenses {
            license {
                name.set(rootProject.ext.get("POM_LICENCE_NAME").toString())
                url.set(rootProject.ext.get("POM_LICENCE_URL").toString())
            }
        }
        developers {
            developer {
                id.set(rootProject.ext.get("POM_DEVELOPER_ID").toString())
                name.set(rootProject.ext.get("POM_DEVELOPER_NAME").toString())
            }
        }
        scm {
            connection.set(rootProject.ext.get("POM_SCM_CONNECTION").toString())
            developerConnection.set(rootProject.ext.get("POM_SCM_DEV_CONNECTION").toString())
            url.set(rootProject.ext.get("POM_SCM_URL").toString())
        }
    }
}
