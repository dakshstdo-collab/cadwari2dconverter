plugins {
    alias(libs.plugins.android.application)
}

android {
    namespace = "com.advocate.geetanjali.gupta.app.cadwari2dconverter"
    compileSdk {
        version = release(36)
    }

    defaultConfig {
        applicationId = "com.advocate.geetanjali.gupta.app.cadwari2dconverter"
        minSdk = 24
        targetSdk = 36
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"

        ndk {
            abiFilters += listOf("arm64-v8a")
        }
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

    sourceSets {
        getByName("main") {
            jniLibs.srcDirs("src/main/jniLibs")
        }
    }
}

dependencies {
    implementation(libs.appcompat)
    implementation(libs.material)
    implementation(libs.activity)
    implementation(libs.constraintlayout)

    // PDF viewer - Jetpack official
//    implementation("androidx.pdf:pdf-viewer:1.0.0-beta01")
//    implementation(libs.androidx.pdf.viewer)
    implementation("com.github.mhiew:android-pdf-viewer:3.2.0-beta.3")
//    implementation("io.github.afreakyelf:Pdf-Viewer:2.1.1")
//    implementation("io.github.oothp:android-pdf-viewer:3.2.0-beta06")
    // PNG large map tiled viewer
    implementation("com.davemorrissey.labs:subsampling-scale-image-view:3.10.0")
    // ViewPager2 for tabs
    implementation("androidx.viewpager2:viewpager2:1.1.0")


    testImplementation(libs.junit)
    androidTestImplementation(libs.ext.junit)
    androidTestImplementation(libs.espresso.core)
}