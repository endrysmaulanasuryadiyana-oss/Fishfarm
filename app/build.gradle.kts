plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
    id("org.jetbrains.kotlin.plugin.compose")
}

android {
    namespace = "com.budidaya.ikan"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.budidaya.ikan"
        minSdk = 26
        targetSdk = 35
        versionCode = 1
        versionName = "1.0"
    }

    // Keystore tetap (disertakan di proyek) supaya SHA-1 selalu sama,
    // sehingga login Google Drive tetap berfungsi di setiap build.
    // Ganti dengan keystore rahasia Anda sendiri sebelum rilis ke Play Store.
    signingConfigs {
        create("budidaya") {
            storeFile = file("budidaya.keystore")
            storePassword = "budidaya123"
            keyAlias = "budidaya"
            keyPassword = "budidaya123"
        }
    }

    buildTypes {
        debug {
            signingConfig = signingConfigs.getByName("budidaya")
        }
        release {
            isMinifyEnabled = false
            signingConfig = signingConfigs.getByName("budidaya")
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
        compose = true
    }
    lint {
        abortOnError = false
        checkReleaseBuilds = false
    }
}

dependencies {
    val composeBom = platform("androidx.compose:compose-bom:2024.12.01")
    implementation(composeBom)
    implementation("androidx.compose.ui:ui")
    implementation("androidx.compose.ui:ui-tooling-preview")
    implementation("androidx.compose.material3:material3")
    implementation("androidx.compose.material:material-icons-extended")
    debugImplementation("androidx.compose.ui:ui-tooling")

    implementation("androidx.core:core-ktx:1.15.0")
    implementation("androidx.activity:activity-compose:1.9.3")
    implementation("androidx.lifecycle:lifecycle-runtime-ktx:2.8.7")
    implementation("androidx.lifecycle:lifecycle-viewmodel-compose:2.8.7")

    // Login Google + izin Google Drive
    implementation("com.google.android.gms:play-services-auth:21.3.0")

    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.9.0")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-play-services:1.9.0")
}
