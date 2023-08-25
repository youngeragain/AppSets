plugins {
    id("kotlin-kapt")
    kotlin("jvm")
}

group = "com.app"
version = "0.0.1-SNAPSHOT"

repositories {
    mavenCentral()
}
configurations.all {
    //exclude ("org.springframework.cloud")
    //exclude ("org.springframework.boot")
}
dependencies {
    implementation(project(":Share"))
    /*        implementation("org.springframework.cloud:spring-cloud-starter-zookeeper-config")*/
    implementation("org.springframework.cloud:spring-cloud-starter-zookeeper-discovery")
    // https://mvnrepository.com/artifact/org.jetbrains.kotlin/kotlin-reflect
    //runtimeOnly("org.jetbrains.kotlin:kotlin-reflect:1.6.10")
    implementation ("com.jakewharton.retrofit:retrofit2-kotlin-coroutines-adapter:0.9.2")
    api("com.squareup.retrofit2:retrofit:2.9.0")
    implementation("org.springframework:spring-context:5.3.16")
    implementation ("com.squareup.moshi:moshi-kotlin:1.13.0")
    //implementation 'com.squareup.moshi:moshi:1.11.0'
    implementation ("com.squareup.retrofit2:converter-moshi:2.9.0")
    //implementation 'com.squareup.retrofit2:converter-scalars:2.9.0'
    kapt("com.squareup.moshi:moshi-kotlin-codegen:1.13.0")
    implementation ("com.squareup.okhttp3:logging-interceptor:5.0.0-alpha.2")
    implementation ("com.squareup.moshi:moshi:1.11.0")
}
