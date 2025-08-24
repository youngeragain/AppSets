import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    alias(libs.plugins.android.test)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.baselineprofile)
}

android {
    namespace = "xcj.app.baselineprofile"
    compileSdk = 36
    defaultConfig {
        minSdk = 28
        targetSdk = 36

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    targetProjectPath = ":app"


    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }

    kotlin {
        jvmToolchain(17)
        compilerOptions.jvmTarget.set(JvmTarget.JVM_17)
    }
}

// This is the configuration block for the Baseline Profile plugin.
// You can specify to run the generators on a managed devices or connected devices.
baselineProfile {
    useConnectedDevices = true
}

dependencies {
    implementation(libs.androidx.junit)
    implementation(libs.androidx.espresso.core)
    implementation(libs.androidx.uiautomator)
    implementation(libs.androidx.benchmark.macro.junit4)
}

androidComponents {
    onVariants { v ->
        val artifactsLoader = v.artifacts.getBuiltArtifactsLoader()
        v.instrumentationRunnerArguments.put(
            "targetAppId",
            v.testedApks.map { artifactsLoader.load(it)?.applicationId ?: "" }
        )
    }
}