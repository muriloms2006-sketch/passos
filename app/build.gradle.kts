plugins {
    alias(libs.plugins.android.application)
}

android {
    namespace = "com.app.meuspassos"
    compileSdk {
        version = release(37)
    }

    defaultConfig {
        applicationId = "com.app.meuspassos"
        minSdk = 24
        targetSdk = 37
        versionCode = 1
        versionName = "1.0"
    }

    buildTypes {
        release {
            optimization {
                enable = false
            }
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
}

dependencies {
    implementation(libs.material)
    testImplementation(libs.junit)
}