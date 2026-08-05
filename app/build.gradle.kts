plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.compose.compiler)
    alias(libs.plugins.ksp)
}

android {
    namespace = "com.wit.jasonfagerberg.nightsout"
    compileSdk = 36
    buildFeatures { compose = true }
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

// match AGP's Java 11 compile target; built-in Kotlin used to align this for us
kotlin {
    compilerOptions {
        jvmTarget.set(org.jetbrains.kotlin.gradle.dsl.JvmTarget.JVM_11)
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
    implementation(libs.androidx.room.runtime)
    implementation(libs.androidx.room.ktx)
    ksp(libs.androidx.room.compiler)
    implementation(libs.androidx.datastore.preferences)
    implementation(libs.koin.android)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    // Compose
    implementation(platform(libs.compose.bom))
    implementation(libs.material3)
    debugImplementation(libs.ui.tooling)
    debugImplementation(libs.ui.tooling.preview)
    debugImplementation(libs.ui.test.manifest)
    implementation(libs.lifecycle.viewmodel.compose)
    implementation(libs.androidx.activity.compose)
    implementation("androidx.compose.material:material-icons-extended:1.7.8")
    implementation(libs.navigation.compose)
}
