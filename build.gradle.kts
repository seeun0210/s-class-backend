plugins {
    java
    id("org.springframework.boot") version "4.0.3"
    id("io.spring.dependency-management") version "1.1.7"
    kotlin("jvm") version "2.2.21"
    kotlin("plugin.spring") version "2.2.21"
    kotlin("plugin.jpa") version "2.2.21"
    kotlin("kapt") version "2.2.21"
    id("org.jlleitschuh.gradle.ktlint") version "14.1.0"
    id("org.jetbrains.kotlinx.kover") version "0.9.1"
}

repositories { mavenCentral() }

tasks.bootJar { enabled = false }

tasks.register<Exec>("installGitHooks") {
    commandLine("git", "config", "core.hooksPath", ".githooks")
}

tasks.named("build") {
    dependsOn("installGitHooks")
}

subprojects {
    apply(plugin = "java")
    apply(plugin = "org.springframework.boot")
    apply(plugin = "io.spring.dependency-management")
    apply(plugin = "org.jetbrains.kotlin.jvm")
    apply(plugin = "org.jetbrains.kotlin.plugin.spring")
    apply(plugin = "org.jetbrains.kotlin.plugin.jpa")
    apply(plugin = "org.jetbrains.kotlin.kapt")
    apply(plugin = "org.jlleitschuh.gradle.ktlint")
    apply(plugin = "org.jetbrains.kotlinx.kover")

    group = "com.sclass"
    version = "0.0.1-SNAPSHOT"

    java {
        sourceCompatibility = JavaVersion.VERSION_21
        targetCompatibility = JavaVersion.VERSION_21
    }

    kotlin {
        jvmToolchain(21)
    }

    repositories { mavenCentral() }

    dependencies {
        implementation("org.jetbrains.kotlin:kotlin-reflect")
        implementation("com.fasterxml.jackson.module:jackson-module-kotlin")
        testImplementation("org.springframework.boot:spring-boot-starter-test")
        testImplementation("com.h2database:h2")
    }

    tasks.withType<Test> {
        useJUnitPlatform()
    }

    ktlint {
        version.set("1.5.0")
        android.set(false)
        outputToConsole.set(true)
    }
}

dependencies {
    subprojects.forEach { kover(it) }
}

kover {
    reports {
        total {
            xml {
                onCheck = false
            }
            html {
                onCheck = false
            }
            filters {
                excludes {
                    classes(
                        // Application main
                        "*Application",
                        "*Application\$Companion",
                        // Config & Properties
                        "*Config",
                        "*Config\$*",
                        "*Properties",
                        // DTO (Request, Response, data class)
                        "*Request",
                        "*Response",
                        "*Result",
                        "*Info",
                        // Entity & Enum & VO
                        "*.domain.*Entity",
                        "*.domain.BaseTimeEntity",
                        "*.domain.AuthProvider",
                        "*.domain.Platform",
                        "*.domain.Role",
                        "*.domain.Grade",
                        "*.domain.FileType",
                        "*.domain.TokenType",
                        // Exception & ErrorCode
                        "*Exception",
                        "*ErrorCode",
                        // Repository (Spring Data interface)
                        "*Repository",
                    )
                    packages(
                        // Test support
                        "*.config",
                    )
                }
            }
        }
    }
}
