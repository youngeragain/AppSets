
plugins {
    //alias(libs.plugins.android.library)
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.ksp)

}

android {
    namespace = "xcj.app.screen_share"
    compileSdk = 36

    defaultConfig {
        applicationId = "xcj.app.screen_share"
        minSdk = 24

        // versionCode = 1
        // versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        proguardFiles("consumer-rules.pro")
        //consumerProguardFiles("consumer-rules.pro")
    }

    buildTypes {
        release {
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
        debug {
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }

    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
    buildFeatures {
        compose = true
    }
    /*kotlin {
        jvmToolchain(17)
    }*/
    kotlinOptions {
        jvmTarget = "17"
    }
    /*composeOptions {
        kotlinCompilerExtensionVersion = "1.5.3"
    }*/
}

dependencies {
    val isLib = false
    if (isLib) {
        compileOnly(project(":starter"))
        compileOnly(project(":compose_share"))
    } else {
        implementation(project(":starter"))
        implementation(project(":compose_share"))
    }

    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.lifecycle.livedata.ktx)
    implementation(libs.androidx.lifecycle.viewmodel.ktx)

    implementation(libs.coil)
    implementation(libs.coil.compose)

    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)


    implementation(libs.ffmpeg.kit.full)
}