// AGP 9 has built-in Kotlin support, so the app module does not apply kotlin-android.
// It is declared here only to pin the Kotlin Gradle Plugin version via the version catalog.
plugins {
    alias(libs.plugins.android.application) apply false
    alias(libs.plugins.kotlin.android) apply false
    alias(libs.plugins.kotlin.compose) apply false
    alias(libs.plugins.ksp) apply false
}
