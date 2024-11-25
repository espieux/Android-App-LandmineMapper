plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    id("org.jetbrains.kotlin.kapt")
}

android {
    namespace = "com.devinci.landminemapper"
    compileSdk = 34

    defaultConfig {
        applicationId = "com.devinci.landminemapper"
        minSdk = 24
        targetSdk = 34
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"

        // Fetch the API key from environment variables
        val apiKey: String? = System.getenv("MAPS_API_KEY")
        // Set manifest placeholders
        manifestPlaceholders["MAPS_API_KEY"] = apiKey ?: ""
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
}

dependencies {

    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.appcompat)
    implementation(libs.material)
    implementation(libs.androidx.activity)
    implementation(libs.androidx.constraintlayout)
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)

    // Google Maps SDK
    implementation(libs.play.services.maps)

    // Location services for getting user location
    implementation(libs.play.services.location)

    // Room components (for local database)
    implementation("androidx.room:room-runtime:2.5.2")
    kapt("androidx.room:room-compiler:2.5.2") // Kapt for Room annotation processing
    implementation("androidx.room:room-ktx:2.5.2") // Room with Kotlin Extensions

    // Lifecycle components (ViewModel, LiveData)
    implementation("androidx.lifecycle:lifecycle-viewmodel-ktx:2.6.1")
    implementation("androidx.lifecycle:lifecycle-livedata-ktx:2.6.1")

    // Coroutines (for background tasks)
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.7.3")

    // Photo Picker for Android 14+
    implementation("androidx.activity:activity-compose:1.6.0-alpha03") // Activity result for the Photo Picker

    implementation (libs.picasso)
}
