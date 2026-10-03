plugins {
    alias(libs.plugins.android.application)
}

android {
    namespace = "com.beastblocks.provisionerjatt.examplejava"
    compileSdk = 36

    defaultConfig {
        applicationId = "com.beastblocks.provisionerjatt.java"
        minSdk = 26
        targetSdk = 36
        versionCode = 5
        versionName = "1.2.3"
        ndk {
            abiFilters += listOf("armeabi-v7a", "arm64-v8a")
        }
    }
    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro")
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
    packaging {
        jniLibs {
            excludes += setOf("**/x86/**", "**/x86_64/**")
        }
    }
}

dependencies {
    implementation("com.beastblocks:provisioner-jatt:1.2.3")
    implementation("androidx.activity:activity:1.8.0")
}
