plugins { id("com.android.application") }

android {
    namespace = "com.bhekanik.homedisplay"
    compileSdk = 35
    defaultConfig {
        applicationId = "com.bhekanik.homedisplay"
        minSdk = 29
        targetSdk = 35
        versionCode = 1
        versionName = "0.1.0"
    }
    buildFeatures { buildConfig = true }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
    testOptions { unitTests.all { it.maxParallelForks = 4 } }
}

dependencies { testImplementation("junit:junit:4.13.2") }
