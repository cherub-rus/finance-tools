
plugins {
    id("finance-tools-common")
    kotlin("plugin.serialization") version "1.8.0"
    application
}

version = "1.0-SNAPSHOT"

application {
    mainClass.set("org.cherub.fintools.pdftool.MainKt")
}

dependencies {
    implementation("org.apache.commons:commons-text:1.10.0")
    implementation("org.apache.pdfbox:pdfbox-tools:2.0.27")
}