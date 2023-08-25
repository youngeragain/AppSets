import org.jetbrains.kotlin.kapt3.base.Kapt.kapt

plugins {
    kotlin("jvm")
    id("kotlin-kapt")
}

group = "com.app"
version = "0.0.1-SNAPSHOT"

repositories {
    mavenCentral()
}

dependencies {

    //implementation(project(":Annotations1"))
    implementation(project(":Proccessor1"))
    kapt (project(":Proccessor1"))
    //implementation(kotlin("stdlib"))
}
configurations.all {
    exclude ("org.springframework.cloud")
    exclude ("org.springframework.boot")
}