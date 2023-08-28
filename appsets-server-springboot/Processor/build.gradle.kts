plugins {
    kotlin("jvm")
}

group = "com.app"
version = "0.0.1-SNAPSHOT"

repositories {
    mavenCentral()
}

dependencies {
    //implementation(project(":Annotations1"))
    //implementation(kotlin("stdlib"))
}
configurations.all {
    exclude ("org.springframework.cloud")
    exclude ("org.springframework.boot")
}