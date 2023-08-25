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
    // https://mvnrepository.com/artifact/org.springframework.boot/spring-boot-starter-data-jpa
    implementation("org.springframework.boot:spring-boot-starter-data-jpa")
    runtimeOnly("com.mysql:mysql-connector-j")
    // https://mvnrepository.com/artifact/org.mybatis.spring.boot/mybatis-spring-boot-starter
    implementation("org.mybatis.spring.boot:mybatis-spring-boot-starter:3.0.2")

}