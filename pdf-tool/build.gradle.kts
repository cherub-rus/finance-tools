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
    mainClass.set("org.cherub.fintools.pdftool.MainKt")
}

dependencies {
    implementation("org.apache.commons:commons-text:1.15.0")
    implementation("org.apache.pdfbox:pdfbox-tools:2.0.37")
// TODO pdfbox 3
//    implementation("org.apache.pdfbox:pdfbox-tools:3.0.8")
    implementation(project(":common"))
}