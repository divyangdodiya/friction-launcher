plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.kotlin.serialization)
    alias(libs.plugins.ksp)
    alias(libs.plugins.hilt)
}

// CI passes the run number so every published APK upgrades the previous one in place.
val ciVersionCode = providers.environmentVariable("VERSION_CODE").orNull?.toIntOrNull() ?: 1

android {
    namespace = "com.dd.friction"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.dd.friction"
        minSdk = 26
        targetSdk = 35
        versionCode = ciVersionCode
        versionName = "1.0.$ciVersionCode"
    }

    // Official releases are signed in CI with a private key from repository secrets.
    // Local builds and forks fall back to the standard Android debug key.
    val releaseKeystore = providers.environmentVariable("SIGNING_KEYSTORE_PATH").orNull
    val hasReleaseKey = !releaseKeystore.isNullOrBlank()

    signingConfigs {
        if (hasReleaseKey) {
            create("release") {
                storeFile = file(releaseKeystore!!)
                storePassword = providers.environmentVariable("SIGNING_STORE_PASSWORD").orNull
                keyAlias = providers.environmentVariable("SIGNING_KEY_ALIAS").orNull
                keyPassword = providers.environmentVariable("SIGNING_KEY_PASSWORD").orNull
            }
        }
    }

    buildTypes {
        release {
            isMinifyEnabled = true
            isShrinkResources = true
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro",
            )
            signingConfig = signingConfigs.getByName(if (hasReleaseKey) "release" else "debug")
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
        // Sideloaded personal app: Play Store target SDK deadlines do not apply.
        disable += setOf("ExpiredTargetSdkVersion", "OldTargetApi")
    }
}

dependencies {
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.activity.compose)
    implementation(libs.androidx.lifecycle.runtime.compose)
    implementation(libs.androidx.lifecycle.viewmodel.compose)

    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.compose.ui)
    implementation(libs.androidx.compose.foundation)
    implementation(libs.androidx.compose.material3)

    implementation(libs.androidx.navigation.compose)
    implementation(libs.androidx.hilt.navigation.compose)
    implementation(libs.androidx.datastore.preferences)

    implementation(libs.kotlinx.coroutines.android)
    implementation(libs.kotlinx.serialization.json)

    implementation(libs.hilt.android)
    ksp(libs.hilt.compiler)

    testImplementation(libs.junit)
}
