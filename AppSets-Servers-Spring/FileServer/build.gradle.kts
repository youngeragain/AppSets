import org.gradle.kotlin.dsl.withType
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    alias(libs.plugins.kotlin.jvm)
    alias(libs.plugins.kotlin.spring)
    alias(libs.plugins.spring.dependency.management)
    alias(libs.plugins.spring.boot)
    alias(libs.plugins.graalvm.buildtools.native)
    alias(libs.plugins.kapt)
}

group = "xcj.app.appsets.server.file"
version = "0.0.1-SNAPSHOT"

dependencyManagement {
    imports {
        mavenBom(libs.spring.cloud.dependency.bom.get().toString())
    }
}

kotlin {
    compilerOptions {
        freeCompilerArgs = listOf("-Xjsr305=strict")
        //jvmTarget = "17"
    }
}

dependencies {

    implementation(project(":Share"))

    implementation(libs.spring.boot.web)

    kapt(libs.spring.boot.configuration.processor)

    developmentOnly(libs.spring.boot.devtools)
    testImplementation(libs.kotlin.test.junit5)
    testRuntimeOnly(libs.junit.platform.launcher)
    testImplementation(libs.spring.boot.test)
}

/*
tasks.withType<KotlinCompile> {
    kotlinOptions {
        freeCompilerArgs = listOf("-Xjsr305=strict")
        jvmTarget = "17"
    }
}
*/

tasks.withType<Test> {
    useJUnitPlatform()
}