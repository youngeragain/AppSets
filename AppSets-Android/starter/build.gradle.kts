import com.android.build.api.dsl.LibraryExtension

plugins {
    alias(libs.plugins.android.library)
    alias(libs.plugins.google.ksp)
    alias(libs.plugins.kotlin.parcelize)
    alias(libs.plugins.kotlin.android)
}

configure<LibraryExtension> {
    namespace = "xcj.app.starter"
    compileSdk = libs.versions.android.compileSdk.get().toInt()
    defaultConfig {
        //applicationId "xcj.app.stater"
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
    //implementation(project(":purple_native"))
    api(libs.androidx.appcompat)
    api(libs.androidx.core.ktx)
    api(libs.google.gson)

    api(libs.retrofit.converter.gson)
    api(libs.okhttp.logging)
    api(libs.retrofit.kotlin.coroutines.adapter)

    api(libs.google.zxing)
    implementation(libs.androidx.core.ktx)

    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
}