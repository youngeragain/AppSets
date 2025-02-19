plugins {
    alias(libs.plugins.android.library)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)

    alias(libs.plugins.ksp)
    //alias(libs.plugins.hilt)
    // this version matches your Kotlin version
}

apply(from = "custom_build.gradle")

android {
    namespace = "xcj.app.appsets"
    compileSdk = 35
    sourceSets {
        // Encapsulates configurations for the main source set.
        getByName("main") {
            // Changes the directory for Java sources. The default directory is
            // "src/main/java".
            java.srcDirs("kotlin")

            // When you list multiple directories, Gradle uses all of them to collect
            // sources. You should avoid specifying a directory which is a parent to one
            // or more other directories you specify.
            //res.srcDirs = ["other/res1", "other/res2"]

            // For each source set, you can specify only one Android manifest.
            // The following points Gradle to a different manifest for this source set.
            //manifest.srcFile "other/AndroidManifest.xml"
        }

        // Create additional blocks to configure other source sets.
        getByName("androidTest") {

            // If all the files for a source set are located under a single root
            // directory, you can specify that directory using the setRoot property.
            // When gathering sources for the source set, Gradle looks only in locations
            // relative to the root directory you specify. For example, after applying
            // the configuration below for the androidTest source set, Gradle looks for
            // Java sources only in the src/tests/java/ directory.
            setRoot("src/tests")
        }
    }
    defaultConfig {
        //applicationId = "xcj.app.appsets"
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
            //consumerProguardFiles("consumer-rules.pro")
        }
        debug {
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
            //consumerProguardFiles("consumer-rules.pro")
        }
    }

    /*composeOptions {
        kotlinCompilerExtensionVersion = "1.5.3"
    }*/

    buildFeatures {
        compose = true
        buildConfig = false
        aidl = true
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
    compileOnly(project(":starter"))
    compileOnly(project(":io"))
    compileOnly(project(":compose_share"))

    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.lifecycle.livedata.ktx)
    implementation(libs.androidx.lifecycle.viewmodel.ktx)

    implementation(libs.androidx.navigation.fragment.ktx)

    implementation(libs.androidx.slice.core)
    implementation(libs.androidx.slice.builders)
    implementation(libs.androidx.slice.builders.ktx)

    implementation(libs.androidx.room)
    implementation(libs.androidx.room.runtime)
    ksp(libs.androidx.room.compiler)

    implementation(libs.androidx.media3.exoplayer)
    implementation(libs.androidx.media3.ui)
    implementation(libs.androidx.media3.session)
    implementation(libs.androidx.media3.common)

    implementation(libs.androidx.camera.core)
    implementation(libs.androidx.camera.camera2)
    implementation(libs.androidx.camera.lifecycle)
    implementation(libs.androidx.camera.view)
    implementation(libs.androidx.camera.mlkit.vision)

    implementation(libs.androidx.work.runtime.ktx)
    implementation(libs.androidx.core.splashscreen)

    /*    implementation(libs.moshi)
        implementation(libs.moshi.kotlin)
        ksp(libs.moshi.kotlin.codegen)

        implementation(libs.retrofit.converter.moshi)*/
    implementation(libs.retrofit)
    implementation(libs.retrofit.converter.gson)
    implementation(libs.okhttp.logging)
    implementation(libs.retrofit.kotlin.coroutines.adapter)

    implementation(libs.coil)
    implementation(libs.coil.network.okhttp)
    implementation(libs.coil.compose)
    implementation(libs.coil.gif)
    implementation(libs.coil.video)

    implementation(libs.google.barcode.scanning)
    implementation(libs.rabbit.amqp)
    implementation(libs.zxing)
    implementation(libs.haze)
    implementation(libs.haze.materials)

    //implementation(libs.hilt.android)
    //implementation(libs.hilt.lifecycle.viewmodel)
    //ksp(libs.hilt.android.complier)

    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.appcompat)
    implementation(libs.androidx.annotation)
    implementation(libs.androidx.recyclerview)
    implementation(libs.androidx.activity.ktx)

    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
}