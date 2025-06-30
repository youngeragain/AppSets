import java.util.Properties

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.baselineprofile)
    alias(libs.plugins.ksp)
    //alias(libs.plugins.appsets.plugin)
}

apply(from = "custom_build.gradle")

android {
    namespace = "xcj.app.container"
    compileSdk = 35
    signingConfigs {
        getKeystoreProperties()?.let { keystoreProperties ->
            create("release") {
                keyAlias = keystoreProperties["keyAlias"] as String
                keyPassword = keystoreProperties["keyPassword"] as String
                storeFile = file(keystoreProperties["storeFile"] as String)
                storePassword = keystoreProperties["storePassword"] as String
            }
        }

        /*create("debug") {

        }*/
    }
    defaultConfig {
        applicationId = "xcj.app.container"
        minSdk = 24
        targetSdk = 35
        versionCode = 20250630
        versionName = "3.2025.06.30"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        //consumerProguardFiles("consumer-rules.pro")

        ndk {
            //noinspection ChromeOsAbiSupport
            abiFilters.add("arm64-v8a")
        }
    }

    buildTypes {
        release {
            isShrinkResources = true
            isMinifyEnabled = true
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
            runCatching {
                signingConfigs.getByName("release")
            }.onSuccess {
                signingConfig = it
            }.onFailure {
                println("Release signing config not found!")
            }
        }
        debug {
            isShrinkResources = false
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }

    }

    buildFeatures {
        compose = true
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
    /*kotlin {
        jvmToolchain(17)
    }*/

    kotlinOptions {
        jvmTarget = "17"
    }

    packaging {
        resources {
            excludes.addAll(
                listOf(
                    "/META-INF/INDEX.LIST",
                    "/META-INF/io.netty.versions.properties",
                    "/META-INF/{AL2.0,LGPL2.1}",
                    "/META-INF/*.version",
                    "/META-INF/*.textproto",
                    "/META-INF/*.kotlin_module",
                    "/META-INF/com/android/build/grale/*.properties",
                    "/*.json",
                    "/*.properties"
                )
            )
        }
    }
}

dependencies {
    implementation(project(":starter"))
    implementation(project(":io"))
    implementation(project(":appsets"))
    implementation(project(":compose_share"))
    implementation(project(":launcher"))
    implementation(project(":share"))
    implementation(project(":proxy"))

    baselineProfile(project(":baselineprofile"))
    implementation(libs.androidx.profileinstaller)

    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
}

fun getKeystoreProperties(): Properties? {
    val keystorePropertiesFile = rootProject.file("keystore.properties")
    if (keystorePropertiesFile.exists()) {
        val keystoreProperties = Properties()
        keystoreProperties.load(keystorePropertiesFile.inputStream())
        return keystoreProperties
    }
    return null
}