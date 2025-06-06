plugins {
    alias(libs.plugins.android.application)
    id ("com.google.gms.google-services")
}

android {
    namespace = "com.example.bt10"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.example.bt10"
        minSdk = 24
        targetSdk = 34
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
    buildFeatures {
        viewBinding = true
    }
}

dependencies {

    implementation(libs.appcompat)
    implementation(libs.material)
    implementation(libs.activity)
    implementation(libs.constraintlayout)
    testImplementation(libs.junit)
    androidTestImplementation(libs.ext.junit)
    androidTestImplementation(libs.espresso.core)

    implementation (libs.play.services.auth)
    /**firebase dependencies*/
    implementation (libs.firebase.auth)
    implementation (libs.firebase.database)
    implementation (platform(libs.firebase.bom))
    implementation (libs.firebase.database.ktx)
    implementation (libs.firebase.firestore)
    implementation (libs.google.firebase.storage)
    implementation (libs.firebase.ui.database)

    // Network & Retrofit
    implementation (libs.retrofit)
    implementation (libs.converter.gson)
//Gson
    implementation (libs.gson)

    implementation (libs.cloudinary.android)
    implementation (libs.glide)
    annotationProcessor (libs.compiler)
}