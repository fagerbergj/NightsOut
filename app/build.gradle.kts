plugins {
    alias(libs.plugins.android.application)
}

android {
    namespace = "com.wit.jasonfagerberg.nightsout"
    compileSdk = 36
    defaultConfig {
        applicationId = "com.wit.jasonfagerberg.nightsout"
        minSdk = 24
        targetSdk = 36
        versionCode = 1904
        versionName = "Rattata"
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }
    buildTypes {
        release {
            isMinifyEnabled = true
            isShrinkResources = true
            proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro")
        }
    }
    testOptions {
        // Converter's init touches android.util.SparseIntArray; JVM tests need stub defaults
        unitTests.isReturnDefaultValues = true
    }
}

dependencies {
    implementation(fileTree(mapOf("dir" to "libs", "include" to listOf("*.jar"))))
    implementation(libs.androidx.viewpager)
    implementation(libs.androidx.appcompat)
    implementation(libs.material)
    // jetified like the calendar AAR; core classes reference support ViewCompat/EdgeEffectCompat
    implementation(files("libs/graphview-4.2.2-androidx.aar"))
    implementation(libs.androidx.constraintlayout)
    testImplementation(libs.junit)
    testImplementation(libs.assertj.core)
    // 2.0.1 AAR run through Jetifier once (upstream ships support-library
    // bytecode); lets us drop android.enableJetifier
    implementation(files("libs/material-calendarview-2.0.1-androidx.aar"))
    // declared runtime-scoped by material-calendarview; needed on compile classpath for AndroidThreeTen.init
    implementation(libs.threetenabp)
    androidTestImplementation(libs.androidx.test.runner)
    androidTestImplementation(libs.androidx.espresso.core)
    implementation(libs.androidx.preference)
}
