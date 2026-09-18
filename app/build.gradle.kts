plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
    id("org.jetbrains.kotlin.plugin.compose")
    id("com.google.gms.google-services")
}

android {
    namespace = "com.dakbit.fortune"
    compileSdk = 36

    defaultConfig {
        applicationId = "com.dakbit.fortune"
        minSdk = 26
        targetSdk = 36
        versionCode = 7
        versionName = "1.0.6"
        buildConfigField("String", "PANGLE_APP_ID", "\"8896839\"")
        buildConfigField("String", "PANGLE_INTERSTITIAL_SLOT_ID", "\"983641203\"")
        buildConfigField("String", "UNITY_GAME_ID", "\"800374063\"")
        buildConfigField("String", "UNITY_INTERSTITIAL_PLACEMENT_ID", "\"BP_Interstitial_Android\"")
    }

    buildTypes {
        debug {
            buildConfigField("String", "CAULY_APP_CODE", "\"CAULY\"")
        }
        release {
            isMinifyEnabled = true
            isShrinkResources = true
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro",
            )
            buildConfigField("String", "CAULY_APP_CODE", "\"hKFo8djF\"")
        }
    }

    buildFeatures {
        compose = true
        buildConfig = true
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }

    kotlinOptions {
        jvmTarget = "17"
    }

    packaging {
        resources.excludes += "/META-INF/{AL2.0,LGPL2.1}"
    }
}

dependencies {
    val composeBom = platform("androidx.compose:compose-bom:2025.02.00")
    implementation(composeBom)

    implementation("androidx.core:core-ktx:1.16.0")
    implementation("androidx.activity:activity-compose:1.10.1")
    implementation("androidx.fragment:fragment-ktx:1.8.8")
    implementation("androidx.compose.ui:ui")
    implementation("androidx.compose.ui:ui-tooling-preview")
    implementation("androidx.compose.foundation:foundation")
    implementation("androidx.compose.material3:material3")
    implementation("androidx.lifecycle:lifecycle-runtime-compose:2.9.1")
    implementation("com.google.android.gms:play-services-ads-identifier:17.0.0")
    implementation("com.google.android.gms:play-services-appset:16.0.0")
    implementation("com.fsn.cauly:cauly-sdk:3.5.46")
    implementation("com.pangle.global:pag-sdk:7.9.0.9")
    implementation("com.unity3d.ads:unity-ads:4.16.6")

    implementation(platform("com.google.firebase:firebase-bom:33.12.0"))
    implementation("com.google.firebase:firebase-messaging")

    debugImplementation("androidx.compose.ui:ui-tooling")
    debugImplementation("androidx.compose.ui:ui-test-manifest")
}
