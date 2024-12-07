plugins {
    id("com.android.application")
    id("com.google.gms.google-services")


}


android {
    namespace = "com.example.geominder"
    compileSdk = 34


    defaultConfig {
        applicationId = "com.example.geominder"
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
}



dependencies {


    implementation(libs.appcompat)
    implementation(libs.material)
    implementation(libs.activity)
    implementation(libs.constraintlayout)
    testImplementation(libs.junit)
    androidTestImplementation(libs.ext.junit)
    androidTestImplementation(libs.espresso.core)
    implementation(platform("com.google.firebase:firebase-bom:33.6.0"))
    implementation("com.google.firebase:firebase-analytics")
    implementation ("com.google.firebase:firebase-auth:22.1.1")
    implementation ("com.google.firebase:firebase-firestore:24.6.1")
    implementation ("com.google.android.gms:play-services-auth:20.1.0")
    implementation ("com.google.android.gms:play-services-maps:18.0.0")
    implementation ("com.google.android.gms:play-services-location:18.0.0")
    implementation ("com.google.android.libraries.places:places:3.0.0")
    implementation ("com.squareup.okhttp3:okhttp:4.11.0")
    implementation ("com.caverock:androidsvg:1.4")
    implementation ("com.google.firebase:firebase-messaging:23.1.1")
    implementation ("androidx.work:work-runtime:2.8.0")
    implementation ("com.caverock:androidsvg:1.4")
    implementation ("com.google.android.material:material:1.8.0")
    implementation ("androidx.cardview:cardview:1.0.0")
}

