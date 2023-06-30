
plugins {
    id("finance-tools-common")
    kotlin("plugin.serialization") version "1.8.0"
    application
}

version = "1.0-SNAPSHOT"

application {
    mainClass.set("org.cherub.fintools.txttool.MainKt")
}

dependencies {
    implementation(project(":config"))
}