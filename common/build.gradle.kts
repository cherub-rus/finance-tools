plugins {
    kotlin("jvm") version "2.4.21"
    kotlin("plugin.serialization") version "2.4.21"
    application
}

group = "org.cherub.fintools"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
}

kotlin {
    jvmToolchain(21)
}

application {
    mainClass.set("org.cherub.fintools.MainKt")
}

dependencies {
    implementation("org.apache.commons:commons-text:1.15.0")
    implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.13.0")
}