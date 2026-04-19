import com.android.build.api.dsl.LibraryExtension

plugins {
    alias(libs.plugins.android.library)
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.google.ksp)
    alias(libs.plugins.kotlin.android)
}

configure<LibraryExtension> {
    namespace = "xcj.app.launcher"
    compileSdk = libs.versions.android.compileSdk.get().toInt()

    defaultConfig {
        //applicationId = "xcj.app.launcher"
        minSdk = libs.versions.android.minSdk.get().toInt()

        // versionCode = 1
        // versionName = "1.0"

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
        sourceCompatibility = JavaVersion.VERSION_21
        targetCompatibility = JavaVersion.VERSION_21
    }
    buildFeatures {
        compose = true
    }

    /*composeOptions {
        kotlinCompilerExtensionVersion = "1.5.3"
    }*/
}

dependencies {
    val isLib = true
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
}