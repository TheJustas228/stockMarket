plugins {
    id("com.android.application") version "8.2.0" apply false
    // Other common plugins if any
}

allprojects {
    repositories {
        google()
        mavenCentral()
        maven { url = uri("https://jitpack.io") }
    }
}