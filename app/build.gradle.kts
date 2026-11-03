import java.util.Properties

plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.plugin.compose")
}

val localProps = Properties().apply {
    val f = rootProject.file("local.properties")
    if (f.exists()) f.inputStream().use { load(it) }
}

android {
    namespace = "com.siowe.app"
    compileSdk = 37 // нужен для clerk-android-ui 1.2.0 (Android 17 = API 37)

    defaultConfig {
        applicationId = "com.siowe.app"
        minSdk = 35      // Android 15
        targetSdk = 36   // работает на Android 15, 16 и 17
        versionCode = 1
        versionName = "1.0"
        buildConfigField(
            "String", "CLERK_PUBLISHABLE_KEY",
            "\"${localProps.getProperty("clerk.publishableKey", "")}\""
        )
    }

    buildFeatures {
        compose = true
        buildConfig = true
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            signingConfig = signingConfigs.getByName("debug") // чтобы release-APK тоже ставился
        }
    }
}

dependencies {
    implementation("com.clerk:clerk-android-api:1.2.0")
    implementation("com.clerk:clerk-android-ui:1.2.0")

    implementation("androidx.core:core-ktx:1.16.0")
    implementation("androidx.activity:activity-compose:1.10.1")
    implementation("androidx.lifecycle:lifecycle-viewmodel-compose:2.11.0")
    implementation("androidx.lifecycle:lifecycle-runtime-compose:2.11.0")
    implementation("androidx.compose.ui:ui:1.11.4")
    implementation("androidx.compose.foundation:foundation:1.11.4")
    implementation("androidx.compose.material3:material3:1.4.0")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.10.2")
}
