import com.android.build.api.dsl.ApplicationExtension

plugins {
    alias(libs.plugins.android.library)
}

configure<ApplicationExtension> {
    namespace = "xcj.app.binder"
    compileSdk = libs.versions.android.compileSdk.get().toInt()
    defaultConfig {
        //applicationId = "xcj.app.binder"
        minSdk = libs.versions.android.minSdk.get().toInt()


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
}



dependencies {
    implementation(project(":starter"))

    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
}

