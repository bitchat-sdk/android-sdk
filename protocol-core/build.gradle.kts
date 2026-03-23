plugins {
    alias(libs.plugins.android.library)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.parcelize)
    id("maven-publish")
    id("signing")
}

android {
    namespace = "io.github.bitchat-sdk.protocol"
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

    publishing {
        singleVariant("release") {
            withSourcesJar()
            withJavadocJar()
        }
    }
}

dependencies {
    implementation(libs.bundles.cryptography)
    implementation(libs.kotlinx.coroutines.android)
}

afterEvaluate {
    publishing {
        publications {
            create<MavenPublication>("release") {
                from(components["release"])
                groupId = rootProject.ext.get("GROUP_ID").toString()
                artifactId = "protocol-core"
                version = rootProject.ext.get("VERSION").toString()

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
        }
        repositories {
            maven {
                name = "sonatype"
                url = if (rootProject.ext.get("VERSION").toString().endsWith("SNAPSHOT"))
                    uri("https://s01.oss.sonatype.org/content/repositories/snapshots/")
                else
                    uri("https://s01.oss.sonatype.org/service/local/staging/deploy/maven2/")
                credentials {
                    username = System.getenv("OSSRH_USERNAME")
                        ?: findProperty("ossrhUsername")?.toString()
                    password = System.getenv("OSSRH_PASSWORD")
                        ?: findProperty("ossrhPassword")?.toString()
                }
            }
        }
    }

    signing {
        val signingKey = System.getenv("SIGNING_KEY")
            ?: findProperty("signingKey")?.toString()
        val signingPassword = System.getenv("SIGNING_PASSWORD")
            ?: findProperty("signingPassword")?.toString()
        if (signingKey != null && signingPassword != null) {
            useInMemoryPgpKeys(signingKey, signingPassword)
            sign(publishing.publications["release"])
        }
    }
}
