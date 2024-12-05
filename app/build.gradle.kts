plugins {
    alias(libs.plugins.androidApplication)
}

android {
    namespace = "com.chinmay.object_detection"
    compileSdk = 34

    defaultConfig {
        applicationId = "com.chinmay.object_detection"
        minSdk = 27
        targetSdk = 34
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }
}

dependencies {

    // CameraX
    implementation("androidx.camera:camera-core:1.3.0-rc01")
    implementation ("androidx.camera:camera-view:1.3.0-rc01")
    implementation ("androidx.camera:camera-camera2:1.3.0-rc01")
    implementation ("androidx.camera:camera-lifecycle:1.3.0-rc01")

    // Labelig
    implementation("com.google.mlkit:image-labeling:17.0.9")

    // ML Kit Object Detection

   // implementation("com.google.mlkit:object-detection:17.0.2")
    implementation(libs.appcompat)
    implementation(libs.material)
    implementation(libs.activity)
    implementation(libs.constraintlayout)
    testImplementation(libs.junit)
    androidTestImplementation(libs.ext.junit)
    androidTestImplementation(libs.espresso.core)
}