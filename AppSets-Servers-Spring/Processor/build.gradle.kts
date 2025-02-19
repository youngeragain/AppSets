plugins {
    alias(libs.plugins.kotlin.jvm)
    alias(libs.plugins.kapt)
}

group = "xcj.app.jvm.processor"
version = "0.0.1-SNAPSHOT"

dependencies {
    implementation(libs.kotlin.stdlib)
}