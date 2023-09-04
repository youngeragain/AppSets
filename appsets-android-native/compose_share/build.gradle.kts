plugins {
    id("com.android.library")
    id("org.jetbrains.kotlin.android")
}

android {
    namespace = "xcj.app.compose_share"
    compileSdk = 34

    defaultConfig {
        minSdk = 24
        targetSdk = 34
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        consumerProguardFiles("consumer-rules.pro")
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
            consumerProguardFiles(
                "consumer-rules.pro"
            )
        }
        debug {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
            consumerProguardFiles(
                "consumer-rules.pro"
            )
        }
    }
    buildFeatures {
        compose = true

    }

    composeOptions {
        kotlinCompilerExtensionVersion = "1.4.3"
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
    kotlin {
        jvmToolchain(17)
    }
    kotlinOptions {
        jvmTarget = "17"
    }
}

dependencies {
    compileOnly(project(":core"))
    implementation(platform("androidx.compose:compose-bom:2023.06.00"))
    api("androidx.compose.ui:ui")
    api("androidx.compose.material3:material3:1.1.1")
    api("androidx.compose.ui:ui-tooling-preview")

    androidTestImplementation(platform("androidx.compose:compose-bom:2023.06.00"))
    debugApi("androidx.compose.ui:ui-tooling")
    debugApi("androidx.compose.ui:ui-test-manifest")
    androidTestApi("androidx.compose.ui:ui-test-junit4")

    testImplementation("junit:junit:4.13.2")
    androidTestImplementation("androidx.test.ext:junit:1.1.5")
    androidTestImplementation("androidx.test.espresso:espresso-core:3.5.1")
}