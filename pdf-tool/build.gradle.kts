import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    kotlin("jvm") version "1.8.0"
    kotlin("plugin.serialization") version "1.8.0"
    application
}

group = "me.cherub"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
}

tasks.withType<KotlinCompile>() {
    kotlinOptions.jvmTarget = "1.8"
}

application {
    mainClass.set("org.cherub.fintools.pdftool.MainKt")
}

dependencies {
    implementation("org.apache.commons:commons-text:1.10.0")
    implementation("org.apache.pdfbox:pdfbox-tools:2.0.27")
}