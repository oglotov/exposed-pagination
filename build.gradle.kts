/*
 * Copyright (c) 2024-Present Perracodex. Use of this source code is governed by an MIT license.
 */
import com.vanniktech.maven.publish.SonatypeHost

plugins {
    `java-library`
    signing
    alias(libs.plugins.dokka)
    alias(libs.plugins.kotlin.jvm)
    alias(libs.plugins.kotlin.serialization)
    alias(libs.plugins.vanniktech)
}

group = "io.github.perracodex"
version = "1.0.0"

// Configuration block for all projects in this multi-project build.
allprojects {

    // Define repositories where dependencies are fetched from.
    repositories {
        // Use Maven Central as the primary repository for fetching dependencies.
        mavenCentral()

        // Add the Kotlin JS Wrappers repository from JetBrains Space,
        // required for projects that depend on Kotlin/JS libraries or components.
        maven { url = uri("https://maven.pkg.jetbrains.space/kotlin/p/kotlin/kotlin-js-wrappers") }
    }
}

kotlin {
    jvmToolchain(jdkVersion = 17)

    // Enable explicit API mode for all subprojects.
    // https://github.com/Kotlin/KEEP/blob/master/proposals/explicit-api-mode.md
    // https://kotlinlang.org/docs/whatsnew14.html#explicit-api-mode-for-library-authors
    explicitApi()
}

dependencies {
    implementation(libs.exposed.core)
    implementation(libs.kotlinx.serialization)
    implementation(libs.ktor.server.core)
    testImplementation(kotlin("test"))
}

tasks.test {
    useJUnitPlatform()
}

// https://central.sonatype.com/account
// https://central.sonatype.com/publishing/deployments
// https://vanniktech.github.io/gradle-maven-publish-plugin/central/#automatic-release
mavenPublishing {
    coordinates(
        groupId = group as String,
        artifactId = "exposed-pagination",
        version = version as String
    )

    pom {
        name.set("Exposed ORM Pagination")
        description.set("A Kotlin library providing pagination support for the Exposed ORM and Ktor.")
        url.set("https://github.com/perracodex/exposed-pagination")
        licenses {
            license {
                name.set("MIT License")
                url.set("https://opensource.org/licenses/MIT")
            }
        }
        developers {
            developer {
                id.set("perracodex")
                name.set("Perracodex")
                email.set(System.getenv("DEVELOPER_EMAIL"))
                url = "https://github.com/perracodex"
            }
        }
        scm {
            connection.set("scm:git:git://github.com/perracodex/exposed-pagination.git")
            developerConnection.set("scm:git:ssh://github.com:perracodex/exposed-pagination.git")
            url.set("https://github.com/perracodex/exposed-pagination")
        }
    }

    publishToMavenCentral(SonatypeHost.CENTRAL_PORTAL)
    signAllPublications()
}

signing {
    val privateKey: String? = System.getenv("SIGNING_SECRET_KEY")
    val passphrase: String? = System.getenv("SIGNING_PASSPHRASE")

    if (privateKey.isNullOrBlank() || passphrase.isNullOrBlank()) {
        println("SIGNING_SECRET_KEY or SIGNING_PASSPHRASE is not set. Skipping signing.")
    } else {
        useInMemoryPgpKeys(privateKey, passphrase)
        sign(publishing.publications)
    }
}
