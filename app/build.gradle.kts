import java.io.FileInputStream
import java.util.Properties

plugins {
    id("com.android.application")       // Required for Android app
    id("com.google.gms.google-services") // Required for Firebase
}

android {
    namespace = "grig.yeganyan.trackit"
    compileSdk = 36

    defaultConfig {
        applicationId = "grig.yeganyan.trackit"
        minSdk = 26
        targetSdk = 36
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"

        val properties = Properties()
        val localPropertiesFile = rootProject.file("local.properties")
        if (localPropertiesFile.exists()) {
            properties.load(FileInputStream(localPropertiesFile))
        }
        val apiKey = properties.getProperty("GEMINI_API_KEY") ?: ""

        // This creates the variable inside BuildConfig
        buildConfigField("String", "GEMINI_API_KEY", "\"$apiKey\"")
    }
    buildFeatures {
        buildConfig = true
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
}

dependencies {
    // AndroidX and Material
    implementation("androidx.appcompat:appcompat:1.7.0")
    implementation("com.google.android.material:material:1.10.0")
    implementation("androidx.activity:activity-ktx:1.8.2")
    implementation("androidx.constraintlayout:constraintlayout:2.1.4")

    // Firebase BOM (manages versions)
    implementation(platform("com.google.firebase:firebase-bom:33.7.0"))

    // Firebase services
    implementation("com.google.firebase:firebase-analytics")
    implementation("com.google.firebase:firebase-auth")      // Authentication
    implementation("com.google.firebase:firebase-firestore")  // Firestore database

    implementation("com.google.ai.client.generativeai:generativeai:0.9.0")

    // Required for the Java "Future" way of handling background tasks
    implementation("com.google.guava:guava:31.1-android")

    implementation(libs.activity)  // keep your existing libs

    // Testing
    testImplementation("junit:junit:4.13.2")
    androidTestImplementation("androidx.test.ext:junit:1.1.5")
    androidTestImplementation("androidx.test.espresso:espresso-core:3.5.1")
}