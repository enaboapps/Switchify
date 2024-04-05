plugins {
    kotlin("android") version "1.6.21" apply false
    id("com.android.application") version "8.3.1" apply false
    id("com.android.library") version "8.3.1" apply false
    id("com.google.gms.google-services") version "4.4.0" apply false
    id("com.google.firebase.crashlytics") version "2.9.9" apply false
}

val compose_version by extra("1.5.4")
val kotlin_version by extra("1.9.20")

buildscript {
    repositories {
        google()
        mavenCentral()
    }
    dependencies {
        classpath(kotlin("gradle-plugin", version = "1.9.20"))
    }
}

tasks.register<Delete>("clean") {
    delete(rootProject.buildDir)
}