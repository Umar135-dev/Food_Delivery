plugins {
    alias(libs.plugins.android.application)
    id("kotlin-parcelize")
    id("com.google.gms.google-services")
}

android {
    buildFeatures {
        viewBinding = true
    }
    namespace = "com.example.menu_app"
    compileSdk {
        version = release(37)
    }

    defaultConfig {
        applicationId = "com.example.menu_app"
        minSdk = 24
        targetSdk = 37
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    buildTypes {
        release {
            optimization {
                enable = false
            }
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
}

dependencies {
    implementation(libs.androidx.activity.ktx)
    implementation(libs.androidx.appcompat)
    implementation(libs.androidx.constraintlayout)
    implementation(libs.androidx.core.ktx)
    implementation(libs.material)
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(libs.androidx.junit)
    implementation("androidx.core:core-splashscreen:1.0.1")
    implementation("com.facebook.shimmer:shimmer:0.5.0")
    implementation("androidx.dynamicanimation:dynamicanimation:1.0.0")
    implementation("androidx.lifecycle:lifecycle-viewmodel-ktx:2.8.6")
    implementation("androidx.lifecycle:lifecycle-livedata-ktx:2.8.6")
    implementation("androidx.fragment:fragment:1.8.4")
    implementation("androidx.fragment:fragment-ktx:1.8.4")
    implementation(platform("com.google.firebase:firebase-bom:33.7.0"))
    // Firebase Authentication Library
    implementation("com.google.firebase:firebase-auth")
    implementation("com.google.firebase:firebase-firestore")
    implementation("com.google.firebase:firebase-functions")
    // Google sign-in
    implementation("androidx.credentials:credentials:1.3.0")
    implementation("androidx.credentials:credentials-play-services-auth:1.3.0")
    implementation("com.google.android.libraries.identity.googleid:googleid:1.1.1")
    implementation("androidx.lifecycle:lifecycle-runtime-ktx:2.8.7")
    implementation("com.github.bumptech.glide:glide:4.16.0")

}