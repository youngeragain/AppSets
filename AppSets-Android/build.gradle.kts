// Top-level build file where you can add configuration options common to all sub-projects/modules.
plugins {
    alias(libs.plugins.android.application) apply false
    alias(libs.plugins.android.library) apply false
    alias(libs.plugins.android.test) apply false
    alias(libs.plugins.kotlin.android) apply false
    alias(libs.plugins.kotlin.parcelize) apply false
    alias(libs.plugins.kotlin.compose) apply false
    alias(libs.plugins.androidx.baselineprofile) apply false
    alias(libs.plugins.google.ksp) apply false
    alias(libs.plugins.kotlin.multiplatform) apply false
    alias(libs.plugins.compose.multiplatform) apply false
    alias(libs.plugins.composeHotReload) apply false

    alias(libs.plugins.google.android.libraries.mapsplatform.secrets.gradle.plugin) apply false

}

subprojects {
    configurations.all {
        resolutionStrategy {
            force("org.jetbrains:annotations:26.1.0")
        }
    }
}
