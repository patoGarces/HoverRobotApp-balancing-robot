plugins {
    alias(libs.plugins.androidApplication)
    alias(libs.plugins.jetbrainsKotlinAndroid)
    kotlin("kapt")
    id("dagger.hilt.android.plugin")
}

android {
    namespace = "com.example.hoverrobot"
    compileSdk = 34

    defaultConfig {
        applicationId = "com.example.hoverrobot"
        minSdk = 26
        targetSdk = 34
        versionCode = 1
        versionName = "1.0.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
            signingConfig = signingConfigs.getByName("debug")
            buildConfigField("long", "VERSION_CODE", "${defaultConfig.versionCode}")
            buildConfigField("String","VERSION_NAME","\"${defaultConfig.versionName}\"")
        }
        debug {
            buildConfigField("long", "VERSION_CODE", "${defaultConfig.versionCode}")
            buildConfigField("String","VERSION_NAME","\"${defaultConfig.versionName}\"")
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
        buildConfig = true
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
    implementation(libs.android.gif.drawable)

    implementation(libs.kotlinx.serialization.json)

    // Joystick
    implementation(libs.virtualjoystick)

    // Plotter
    implementation("com.github.PhilJay:MPAndroidChart:v3.1.0")

    // DataStore
    implementation(libs.androidx.datastore.preferences)

    // Hilt
    implementation ("com.google.dagger:hilt-android:2.48")
    kapt ("com.google.dagger:hilt-compiler:2.48")
}

kapt {
    correctErrorTypes = true
}

