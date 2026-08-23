plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
    id("org.jetbrains.kotlin.plugin.compose")
}

android {
    namespace = "com.dakbit.fortune"
    compileSdk = 36

    defaultConfig {
        applicationId = "com.dakbit.fortune"
        minSdk = 26
        targetSdk = 36
        versionCode = 3
        versionName = "1.0.2"
    }

    buildTypes {
        debug {
            // Google 공식 테스트 ID
            manifestPlaceholders["admobAppId"] = "ca-app-pub-3940256099942544~3347511713"
            buildConfigField(
                "String",
                "ADMOB_INTERSTITIAL_UNIT_ID",
                "\"ca-app-pub-3940256099942544/1033173712\"",
            )
        }
        release {
            // 실제 AdMob ID (달빛 운세)
            manifestPlaceholders["admobAppId"] = "ca-app-pub-2337539333759239~6358722846"
            buildConfigField(
                "String",
                "ADMOB_INTERSTITIAL_UNIT_ID",
                "\"ca-app-pub-2337539333759239/1816842608\"",
            )
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

    implementation("androidx.core:core-ktx:1.15.0")
    implementation("androidx.activity:activity-compose:1.10.0")
    implementation("androidx.compose.ui:ui")
    implementation("androidx.compose.ui:ui-tooling-preview")
    implementation("androidx.compose.foundation:foundation")
    implementation("androidx.compose.material3:material3")
    implementation("androidx.lifecycle:lifecycle-runtime-compose:2.8.7")
    implementation("com.google.android.gms:play-services-ads:24.2.0")

    debugImplementation("androidx.compose.ui:ui-tooling")
    debugImplementation("androidx.compose.ui:ui-test-manifest")
}
