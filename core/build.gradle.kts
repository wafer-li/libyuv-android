import com.android.build.gradle.*
import com.android.build.gradle.internal.cxx.configure.abiOf
import org.jetbrains.dokka.gradle.DokkaTask
import java.net.URI

plugins {
    id("com.android.library")
    kotlin("android")
    id("org.jetbrains.dokka")
    id("maven-publish")
}

object Maven {
    const val groupId = "io.github.crow-misia.libyuv"
    const val artifactId = "libyuv-android"
    const val name = "libyuv-android"
    const val desc = "LibYuv for Android"
    const val version = "0.23.0"
    const val siteUrl = "https://github.com/crow-misia/libyuv-android"
    const val gitUrl = "https://github.com/crow-misia/libyuv-android.git"
    const val licenseName = "The Apache Software License, Version 2.0"
    const val licenseUrl = "http://www.apache.org/licenses/LICENSE-2.0.txt"
    const val licenseDist = "repo"
    val licenses = arrayOf("Apache-2.0")
}

group = Maven.groupId
version = Maven.version

android {
    buildToolsVersion = "33.0.0"
    compileSdk = 33

    defaultConfig {
        namespace = "io.github.crow_misia.libyuv"
        minSdk = 14
        consumerProguardFiles("consumer-proguard-rules.pro")
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"

        ndk {
            // Specifies the ABI configurations of your native
            // libraries Gradle should build and package with your app.
            abiFilters += listOf("arm64-v8a")
        }
    }

    lint {
        textReport = true
    }

    libraryVariants.all {
        generateBuildConfigProvider?.configure {
            enabled = false
        }
    }

    buildTypes {
        debug {
            isJniDebuggable = false
        }
        release {
            isJniDebuggable = false
        }
    }

    externalNativeBuild {
        ndkBuild {
            path(File("${projectDir}/Android.mk"))
        }
    }
    ndkVersion = "25.1.8937393"

    sourceSets {
        getByName("androidTest").manifest {
            srcFile("src/androidTest/AndroidManifest.xml")
        }
    }

    kotlinOptions {
        freeCompilerArgs = listOf("-Xjsr305=strict", "-module-name", "libyuv-android")
        jvmTarget = "1.8"
        apiVersion = "1.7"
        languageVersion = "1.7"
    }
}

dependencies {
    implementation(AndroidX.annotation)

    androidTestImplementation(AndroidX.test.runner)
    androidTestImplementation(AndroidX.test.rules)
    androidTestImplementation(AndroidX.test.ext.junit.ktx)
    androidTestImplementation(AndroidX.test.ext.truth)
    androidTestImplementation(libs.truth)
}

val sourcesJar by tasks.creating(Jar::class) {
    group = JavaBasePlugin.DOCUMENTATION_GROUP
    description = "Assembles sources JAR"
    archiveClassifier.set("sources")
    from(sourceSets.create("main").allSource)
}

val customDokkaTask by tasks.creating(DokkaTask::class) {
    dokkaSourceSets.getByName("main") {
        noAndroidSdkLink.set(false)
    }
    dependencies {
        plugins(libs.javadoc.plugin)
    }
    inputs.dir("src/main/java")
    outputDirectory.set(buildDir.resolve("javadoc"))
}

val javadocJar by tasks.creating(Jar::class) {
    dependsOn(customDokkaTask)
    group = JavaBasePlugin.DOCUMENTATION_GROUP
    description = "Assembles JavaDoc JAR"
    archiveClassifier.set("javadoc")
    from(customDokkaTask.outputDirectory)
}

afterEvaluate {
    publishing {
        publications {
            create<MavenPublication>("maven") {
                from(components["release"])

                groupId = Maven.groupId
                artifactId = Maven.artifactId

                println("""
                    |Creating maven publication
                    |    Group: $groupId
                    |    Artifact: $artifactId
                    |    Version: $version
                """.trimMargin())

                artifact(sourcesJar)
                artifact(javadocJar)

                pom {
                    name.set(Maven.name)
                    description.set(Maven.desc)
                    url.set(Maven.siteUrl)

                    scm {
                        val scmUrl = "scm:git:${Maven.gitUrl}"
                        connection.set(scmUrl)
                        developerConnection.set(scmUrl)
                        url.set(Maven.gitUrl)
                        tag.set("HEAD")
                    }

                    developers {
                        developer {
                            id.set("crow-misia")
                            name.set("Zenichi Amano")
                            email.set("crow.misia@gmail.com")
                            roles.set(listOf("Project-Administrator", "Developer"))
                            timezone.set("+9")
                        }
                    }

                    licenses {
                        license {
                            name.set(Maven.licenseName)
                            url.set(Maven.licenseUrl)
                            distribution.set(Maven.licenseDist)
                        }
                    }
                }
            }
        }
        repositories {
            maven {
                val releasesRepoUrl = URI("https://oss.sonatype.org/service/local/staging/deploy/maven2")
                val snapshotsRepoUrl = URI("https://oss.sonatype.org/content/repositories/snapshots")
                url = if (Maven.version.endsWith("SNAPSHOT")) snapshotsRepoUrl else releasesRepoUrl
                val sonatypeUsername: String? by project
                val sonatypePassword: String? by project
                credentials {
                    username = sonatypeUsername.orEmpty()
                    password = sonatypePassword.orEmpty()
                }
            }
        }
    }
}

tasks {
    withType<Test> {
        useJUnitPlatform()
        testLogging {
            showStandardStreams = true
            events("passed", "skipped", "failed")
        }
    }
}
