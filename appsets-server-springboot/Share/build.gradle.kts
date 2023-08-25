plugins {
    kotlin("jvm")
}

group = "com.app"
version = "0.0.1-SNAPSHOT"

repositories {
    mavenCentral()
}

configurations.all {

    exclude ("org.springframework.cloud")
    exclude ("org.springframework.boot")
    exclude ("com.fasterxml.jackson.module")
}

dependencies {
    api(kotlin("stdlib"))
    api("org.jetbrains.kotlin:kotlin-reflect")
    api("org.jetbrains.kotlin:kotlin-stdlib-jdk8")
    // https://mvnrepository.com/artifact/org.jetbrains.kotlinx/kotlinx-coroutines-core
    api("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.6.0")

}