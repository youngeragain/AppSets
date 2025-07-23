import org.gradle.kotlin.dsl.withType
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile
import org.springframework.boot.gradle.tasks.aot.ProcessAot

plugins {
    alias(libs.plugins.kotlin.jvm)
    alias(libs.plugins.kotlin.spring)
    alias(libs.plugins.spring.dependency.management)
    alias(libs.plugins.spring.boot)
    alias(libs.plugins.graalvm.buildtools.native)
    alias(libs.plugins.kapt)
}

group = "xcj.app.appsets.server.main"
version = "0.0.1-SNAPSHOT"

dependencyManagement {
    imports {
        mavenBom(libs.spring.cloud.dependency.bom.get().toString())
    }
}

configurations.all {
    /*exclude(
        group = "org.springframework.boot",
        module = "spring-boot-starter-logging"
    )*/
}


kotlin {
    compilerOptions {
        freeCompilerArgs = listOf("-Xjsr305=strict")
        //jvmTarget = "17"
    }
}

dependencies {

    implementation(project(":Share"))
    //implementation(project(":RetrofitStater"))
    implementation(libs.spring.boot.web)
    implementation(libs.spring.boot.validation)
    implementation(libs.spring.boot.data.redis)
    implementation(libs.spring.boot.data.mongodb)
    implementation(libs.spring.boot.amqp)

    //implementation(libs.spring.boot.actuator)
    //implementation(libs.spring.cloud.openfeign)
    //implementation(libs.spring.cloud.zookeeper.config)
    //implementation(libs.spring.cloud.zookeeper.discovery)

    implementation(libs.mybatis.spring.boot.starter)
    testImplementation(libs.mybatis.spring.boot.starter.test)
    runtimeOnly(libs.mybatis.spring.native)
    implementation(libs.java.jwt)
    implementation(libs.gson)
    runtimeOnly(libs.mysql.connector)

    implementation(libs.xml.bind.api)
    implementation(libs.bouncycastle)
    implementation(libs.jsoup)
    implementation(libs.fasterxml.kotlin)
    implementation(libs.qcloud.cos.sts)

    kapt(libs.spring.boot.configuration.processor)

    developmentOnly(libs.spring.boot.devtools)
    testImplementation(libs.kotlin.test.junit5)
    testRuntimeOnly(libs.junit.platform.launcher)
    testImplementation(libs.spring.boot.test)
}


/*tasks.withType<ProcessAot>().configureEach {
    args("--spring.profiles.active=profile-a,profile-b")
}*/

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