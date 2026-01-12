// /app/build.gradle.kts

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
   // id("kotlin-kapt")
    //id("com.android.application")
    //id("org.jetbrains.kotlin.kapt")
    // Thêm plugin safeargs ở đây nếu bạn cần sử dụng, dựa trên file build.gradle.kts của project
    alias(libs.plugins.androidx.navigation.safeargs)
    alias(libs.plugins.ksp)
}

android {
    namespace = "com.example.smartspend2"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.example.smartspend2"
        minSdk = 26
        targetSdk = 35
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
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }

    kotlinOptions {
        jvmTarget = "17"
    }

    buildFeatures {
        viewBinding = true
    }
}

dependencies {
    // Kotlin
    implementation(libs.kotlin.stdlib)

    // Core Android Libraries
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.appcompat)
    implementation(libs.material)
    implementation(libs.androidx.constraintlayout)

    // RecyclerView
    implementation(libs.androidx.recyclerview)

    // Gson for JSON parsing
    implementation(libs.gson)

    // MPAndroidChart for PieChart
    implementation(libs.mpandroidchart)

    // Coroutines
    implementation(libs.kotlinx.coroutines.android)

    // Lifecycle (ViewModel & LiveData)
    implementation("androidx.lifecycle:lifecycle-runtime-ktx:2.6.2")
    implementation("androidx.lifecycle:lifecycle-viewmodel-ktx:2.6.2")

    // Room Database
    implementation(libs.androidx.room.runtime)
    implementation(libs.androidx.room.ktx)
    ksp(libs.androidx.room.compiler)

    // Testing Libraries
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)

    implementation("androidx.navigation:navigation-fragment-ktx:2.8.5")
    implementation("androidx.navigation:navigation-ui-ktx:2.8.5")
}
