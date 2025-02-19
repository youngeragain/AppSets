// Top-level build file where you can add configuration options common to all sub-projects/modules.
plugins {
    alias(libs.plugins.android.application) apply false
    alias(libs.plugins.kotlin.android) apply false
    alias(libs.plugins.kotlin.compose) apply false
    alias(libs.plugins.android.test) apply false
    alias(libs.plugins.baselineprofile) apply false
    alias(libs.plugins.android.library) apply false
    alias(libs.plugins.ksp) apply false

    alias(libs.plugins.google.android.libraries.mapsplatform.secrets.gradle.plugin) apply false

}

/*
tasks.register("clean", Delete) {
    delete rootProject.getLayout().getBuildDirectory()
}*/