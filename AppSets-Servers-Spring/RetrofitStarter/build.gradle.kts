plugins {
    alias(libs.plugins.kotlin.jvm)
    alias(libs.plugins.kotlin.spring)
    alias(libs.plugins.spring.dependency.management)
    alias(libs.plugins.spring.boot)
    alias(libs.plugins.kapt)
}

group = "xcj.app.starter.retrofit"
version = "0.0.1-SNAPSHOT"

dependencyManagement {
    imports {
        mavenBom(libs.spring.cloud.dependency.bom.get().toString())
    }
}

dependencies {
    implementation(project(":Share"))
    implementation(libs.kotlin.reflect)
    //implementation(libs.spring.cloud.zookeeper.config)
    implementation(libs.spring.cloud.zookeeper.discovery)

    //implementation(libs.kotlin.reflect)
    api(libs.retrofit)
    api(libs.retrofit.kotlin.coroutines.adapter)
    implementation(libs.spring.context)
    implementation(libs.moshi)
    implementation(libs.moshi.kotlin)
    implementation(libs.retrofit.converter.moshi)
    kapt(libs.moshi.kotlin.codegen)
    implementation(libs.okhttp.logging)
}
