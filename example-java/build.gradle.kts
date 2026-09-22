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
        versionCode = 3
        versionName = "1.1.1"
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
}

dependencies {
    implementation("com.beastblocks:provisioner-jatt:1.1.1")
    implementation("androidx.activity:activity:1.8.0")
}
