plugins {

    // Google Services plugin (here apply)
    id("com.google.gms.google-services")

    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.android.application)
}

android {
    namespace = "com.example.kampus_bildirim"
    compileSdk {
        version = release(36)
    }

    defaultConfig {
        applicationId = "com.example.kampus_bildirim"
        minSdk = 24
        targetSdk = 36
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

    // Firebase BoM (versiyon yönetimini kolaylaştırır)
    implementation(platform("com.google.firebase:firebase-bom:34.6.0"))

    // Firebase Authentication
    implementation("com.google.firebase:firebase-auth")

    // Cloud Firestore
    implementation("com.google.firebase:firebase-firestore")

    // Storage (fotoğraf yükleme)
    implementation("com.google.firebase:firebase-storage")

    // Messaging (bildirimler)
    implementation("com.google.firebase:firebase-messaging")

    // Konum izinleri
    implementation("com.google.android.gms:play-services-maps:18.2.0")
}