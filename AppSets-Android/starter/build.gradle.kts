plugins {
    alias(libs.plugins.android.library)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.ksp)
}

android {
    namespace = "xcj.app.starter"
    compileSdk = 35
    defaultConfig {
        //applicationId "xcj.app.stater"
        minSdk = 24

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        consumerProguardFiles("consumer-rules.pro")
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
    /*kotlin {
        jvmToolchain(17)
    }*/

    kotlinOptions {
        jvmTarget = "17"
    }
}

dependencies {
    //implementation(project(":purple_native"))
    api(libs.androidx.appcompat)
    api(libs.androidx.core.ktx)
    api(libs.gson)

    api(libs.retrofit.converter.gson)
    api(libs.okhttp.logging)
    api(libs.retrofit.kotlin.coroutines.adapter)

    api(libs.zxing)

    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
}