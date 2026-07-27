plugins {
    id("com.android.application")
}

android {
    namespace = "com.cocosw.formfiller.example"
    compileSdk = 36

    defaultConfig {
        applicationId = "com.cocosw.formfiller.example"
        minSdk = 21
        targetSdk = 36
        versionCode = 1
        versionName = "2.0.0"
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    buildFeatures {
        buildConfig = true
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
}

kotlin {
    jvmToolchain(17)
}

dependencies {
    implementation(project(":library"))
    implementation("com.github.moove-it:fakeit:v0.7") {
        exclude(group = "com.android.support")
    }
    implementation("androidx.core:core-ktx:1.17.0")
    implementation("androidx.appcompat:appcompat:1.7.1")
    implementation("com.google.android.material:material:1.13.0")
    implementation("androidx.constraintlayout:constraintlayout:2.2.1")
}
