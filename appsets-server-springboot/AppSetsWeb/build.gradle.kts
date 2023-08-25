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
    //exclude ("org.springframework.boot")
}
dependencies {
    implementation(project(":Share"))
// https://mvnrepository.com/artifact/org.springframework.boot/spring-boot-starter-thymeleaf
    implementation("org.springframework.boot:spring-boot-starter-thymeleaf:2.7.1")

}