plugins {
    alias(libs.plugins.androidApplication)
    alias(libs.plugins.jetbrainsKotlinAndroid)
    kotlin("kapt")
}

android {
    namespace = "com.example.hoverrobot"
    compileSdk = 34

    defaultConfig {
        applicationId = "com.example.hoverrobot"
        minSdk = 26
        targetSdk = 34
        versionCode = 1
        versionName = "0.2.1"

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
    kotlinOptions {
        jvmTarget = "1.8"
    }
    buildFeatures {
        viewBinding = true
    }
}

dependencies {

    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.appcompat)
    implementation(libs.material)
    implementation(libs.androidx.constraintlayout)
    implementation(libs.androidx.navigation.fragment.ktx)
    implementation(libs.androidx.navigation.ui.ktx)
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)

    // Para cargar gif
    implementation("pl.droidsonroids.gif:android-gif-drawable:1.2.19")

    implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.2.2")

    // Joystick
    implementation("io.github.controlwear:virtualjoystick:1.10.1")

    // Plotter
    implementation("com.github.PhilJay:MPAndroidChart:v3.1.0")

    // DataStore
    implementation(libs.androidx.datastore.preferences)
    
    // Inyeccion de dependencias
    implementation(libs.hilt.android)
//    kapt("com.google.dagger:hilt-android-compiler:2.40.5")
//    implementation("androidx.hilt:hilt-lifecycle-viewmodel:1.0.0-alpha03")
//    kapt("androidx.hilt:hilt-compiler:1.0.0")

}