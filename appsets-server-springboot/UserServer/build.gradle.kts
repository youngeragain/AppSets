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
 /*   implementation(project(":RetrofitStater"))*/
    /*        implementation("org.springframework.cloud:spring-cloud-starter-zookeeper-config")*/
/*    implementation("org.springframework.cloud:spring-cloud-starter-zookeeper-discovery")
    implementation("org.springframework.boot:spring-boot-starter-actuator")*/
    // https://mvnrepository.com/artifact/org.springframework.boot/spring-boot-starter-data-redis
    implementation("org.springframework.boot:spring-boot-starter-data-redis")
// https://mvnrepository.com/artifact/com.auth0/java-jwt
    implementation("com.auth0:java-jwt:4.0.0")
// https://mvnrepository.com/artifact/com.google.code.gson/gson
    implementation("com.google.code.gson:gson:2.9.0")
    runtimeOnly("com.mysql:mysql-connector-j")
    // https://mvnrepository.com/artifact/org.mybatis.spring.boot/mybatis-spring-boot-starter
    implementation("org.mybatis.spring.boot:mybatis-spring-boot-starter:3.0.2")
    // https://mvnrepository.com/artifact/org.springframework.boot/spring-boot-starter-data-mongodb
    implementation("org.springframework.boot:spring-boot-starter-data-mongodb")
// https://mvnrepository.com/artifact/org.springframework.boot/spring-boot-starter-validation
    implementation("org.springframework.boot:spring-boot-starter-validation")

// https://mvnrepository.com/artifact/org.springframework.cloud/spring-cloud-starter-openfeign
    //implementation("org.springframework.cloud:spring-cloud-starter-openfeign:3.1.1")
// https://mvnrepository.com/artifact/jakarta.xml.bind/jakarta.xml.bind-api
    implementation("jakarta.xml.bind:jakarta.xml.bind-api:4.0.0")

    // https://mvnrepository.com/artifact/org.bouncycastle/bcprov-jdk16
    implementation("org.bouncycastle:bcprov-jdk15:1.46")

    implementation("org.jsoup:jsoup:1.16.1")
    implementation("com.fasterxml.jackson.module:jackson-module-kotlin")

// https://mvnrepository.com/artifact/com.qcloud/cos-sts_api
    implementation("com.qcloud:cos-sts_api:3.1.1")

    // https://mvnrepository.com/artifact/org.springframework.boot/spring-boot-starter-amqp
    implementation("org.springframework.boot:spring-boot-starter-amqp:3.1.2")

}

configurations.all {
    exclude ("org.springframework.cloud")
}
