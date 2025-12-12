plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
}

android {
    namespace = "com.example.lifetracker"
    compileSdk = 34  // ← KEEP THIS AS 34 - NO CHANGES!

    defaultConfig {
        applicationId = "com.example.lifetracker"
        minSdk = 24
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
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
    kotlinOptions {
        jvmTarget = "11"
    }

    buildFeatures {
        viewBinding = true
    }
}

dependencies {
    implementation("androidx.core:core-ktx:1.12.0")
    implementation("androidx.appcompat:appcompat:1.6.1")
    implementation("com.google.android.material:material:1.10.0")
    implementation("androidx.constraintlayout:constraintlayout:2.1.4")
    implementation("androidx.activity:activity-ktx:1.8.0")

    // Navigation Components
    implementation("androidx.navigation:navigation-fragment-ktx:2.7.4")
    implementation("androidx.navigation:navigation-ui-ktx:2.7.4")

    // WorkManager for notifications
    implementation("androidx.work:work-runtime-ktx:2.8.1")

    // Preferences for settings
    implementation("androidx.preference:preference-ktx:1.2.1")

    // Lifecycle for ViewModel
    implementation("androidx.lifecycle:lifecycle-viewmodel-ktx:2.7.0")


    testImplementation("junit:junit:4.13.2")
    androidTestImplementation("androidx.test.ext:junit:1.1.5")
    androidTestImplementation("androidx.test.espresso:espresso-core:3.5.1")

    // GSON for data serialization
    implementation("com.google.code.gson:gson:2.10.1")

    // Add for notification channels
    implementation("androidx.core:core:1.12.0")

    implementation("androidx.work:work-runtime-ktx:2.8.1")


}

// ADD THIS SAFETY NET TO BLOCK COMPOSE LIBRARIES
configurations.all {
    exclude(group = "androidx.compose.ui", module = "ui-graphics-android")
    exclude(group = "androidx.compose.ui", module = "ui-graphics")
    exclude(group = "androidx.compose.ui", module = "ui")
}