plugins {
    alias(libs.plugins.androidApplication)
    alias(libs.plugins.jetbrainsKotlinAndroid)
    alias(libs.plugins.compose.compiler)
    kotlin("kapt")
    id("dagger.hilt.android.plugin")
}

android {
    namespace = "com.app.hoverrobot"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.app.hoverrobot"
        minSdk = 29
        targetSdk = 34
        versionCode = 1
        versionName = "2.0.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"

        buildConfigField("String", "VERSION_NAME", "\"${versionName}\"")
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
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }

    kotlinOptions {
        jvmTarget = "17"
    }

    buildFeatures {
        compose = true
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
    implementation(libs.androidx.ui)

    // Dependencias de Jetpack Compose
    implementation(libs.androidx.ui)
    implementation(libs.androidx.material3)
    implementation(libs.androidx.ui.tooling.preview)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.activity.compose)
    implementation(libs.androidx.material.icons.extended)

    // Dependencias adicionales de Compose
    implementation(libs.androidx.runtime.livedata)
    implementation(libs.androidx.foundation)

    // Dependencias para la integración con ViewModel y otras herramientas
    implementation(libs.androidx.lifecycle.viewmodel.compose)
    implementation(libs.androidx.material.icons.core)

    implementation(libs.androidx.ui.tooling.preview.android)
    implementation(libs.androidx.runtime)

    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)

    // Para cargar gif
    implementation(libs.android.gif.drawable)

    implementation(libs.kotlinx.serialization.json)

    // Plotter
    implementation(libs.compose.charts)
//    implementation("com.github.PhilJay:MPAndroidChart:v3.1.0")

    // DataStore
    implementation(libs.androidx.datastore.preferences)

    // Hilt
    implementation(libs.androidx.hilt.navigation.compose)
    implementation (libs.hilt.android)
    debugImplementation(libs.androidx.ui.tooling)
    kapt (libs.hilt.compiler)

    // Compass view
    implementation("com.github.kix2902:CompassView:master-SNAPSHOT")
}

kapt {
    correctErrorTypes = true
}

