plugins {
    kotlin("jvm")
}

group = "com.app"
version = "0.0.1-SNAPSHOT"

repositories {
    mavenCentral()
}

dependencies {
    implementation(project(":Share"))
//    implementation("org.springframework.cloud:spring-boot-starter-amqp")
    implementation("org.springframework.amqp:spring-rabbit")
    // https://mvnrepository.com/artifact/org.springframework.boot/spring-boot-starter-websocket
    implementation("org.springframework.boot:spring-boot-starter-websocket:2.6.7")

}