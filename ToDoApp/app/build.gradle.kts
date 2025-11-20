plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    id("com.google.gms.google-services")
}

android {
    namespace = "com.rakib.to_do_app"
    compileSdk = 34

    defaultConfig {
        applicationId = "com.rakib.to_do_app"
        minSdk = 26
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
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }

    kotlinOptions {
        jvmTarget = "1.8"
    }

    buildFeatures {
        viewBinding = true
    }

    packagingOptions {
        exclude("META-INF/DEPENDENCIES")
        exclude("META-INF/LICENSE")
        exclude("META-INF/LICENSE.txt")
        exclude("META-INF/NOTICE")
        exclude("META-INF/NOTICE.txt")
        exclude("META-INF/androidx.*")
        exclude("META-INF/proguard/androidx-annotations.pro")
    }
}

dependencies {
    implementation(libs.androidx.appcompat)
    implementation(libs.androidx.material)
    implementation(libs.androidx.activity)
    implementation(libs.androidx.constraintlayout)

    // Firebase Bill of Materials (BOM) - USE ONLY ONE BOM VERSION
    implementation(platform("com.google.firebase:firebase-bom:32.7.0"))

    // Firebase dependencies (they will use versions from the BOM)
    implementation("com.google.firebase:firebase-database")        // For profile data (Realtime Database)
    implementation("com.google.firebase:firebase-firestore")       // For tasks and reminders (Firestore)
    implementation("com.google.firebase:firebase-auth")            // For authentication
    implementation("com.google.firebase:firebase-analytics")       // For analytics
            // For profile pictures storage

    // AndroidX
    implementation("androidx.recyclerview:recyclerview:1.3.2")
    implementation("androidx.work:work-runtime:2.9.0")
    implementation("androidx.core:core-ktx:1.12.0")
    implementation("androidx.fragment:fragment-ktx:1.6.2")
    implementation("androidx.lifecycle:lifecycle-viewmodel-ktx:2.7.0")
    implementation("androidx.lifecycle:lifecycle-livedata-ktx:2.7.0")
    implementation("androidx.exifinterface:exifinterface:1.3.6")   // For image processing

    // UI Libraries
    implementation("de.hdodenhof:circleimageview:3.1.0")
    implementation("com.google.android.material:material:1.10.0")

    // Image loading and processing
    implementation("com.github.bumptech.glide:glide:4.16.0")
    annotationProcessor("com.github.bumptech.glide:compiler:4.16.0")

    // Image Picker (easy to use library)


    // Image Compression
    implementation("id.zelory:compressor:3.0.1")

    // Permissions handling
    implementation("com.karumi:dexter:6.2.3")

    // Testing
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)

}