// Top-level build file
// Q2 fix: removed phantom `org.jetbrains.kotlin.plugin.compose` 1.9.24 declaration
// — that plugin only exists in Kotlin 2.0+; app/build.gradle.kts uses old-style
// composeOptions { kotlinCompilerExtensionVersion = "1.5.14" } instead.
plugins {
    id("com.android.application") version "8.5.0" apply false
    id("org.jetbrains.kotlin.android") version "1.9.24" apply false
}
