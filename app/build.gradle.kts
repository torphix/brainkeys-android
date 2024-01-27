plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
}

android {
    namespace = "com.torphix.brainkey"
    compileSdk = 34

    ndkVersion = "26.1.10909125"

    defaultConfig {
        applicationId = "com.torphix.brainkey"
        minSdk = 33
        targetSdk = 34
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        vectorDrawables {
            useSupportLibrary = true
        }

        ndk {
            // Workaround for https://github.com/llvm/llvm-project/issues/65820
            // affecting armeabi-v7a. Skip armeabi-v7a when invoked with
            // -Pskip-armeabi-v7a (e.g., ./gradlew build -Pskip-armeabi-v7a).
            if (project.hasProperty("skip-armeabi-v7a")) {
                abiFilters += listOf("arm64-v8a", "x86_64", "x86")
            }
        }
        externalNativeBuild {
            cmake {
                if (project.hasProperty("only-arm64-v8a")) {
                    cppFlags += listOf(
                        "-O3",
//                        "-march=armv8.2-a+fp16",
                        "-ffast-math",           // Fast Math Optimizations
                        "-flto",                 // Link Time Optimization
                        "-finline-functions",    // Encourage Function Inlining
                        "-ftree-vectorize",      // Enable Vectorization
                        "-fno-rtti",             // Disable RTTI if not used
                    )
                    cFlags += listOf(
                        "-O3",
//                        "-march=armv8.2-a+fp16",
                        "-ffast-math",
                        "-flto",
                        "-finline-functions",
                        "-ftree-vectorize",
                        "-fno-rtti",
                    )
                } else {
                    cppFlags += listOf(
                        "-O3",
//                        "-mfpu=neon-vfpv4",      // NEON optimizations for other ABIs
                        "-ffast-math",
                        "-flto",
                        "-finline-functions",
                        "-ftree-vectorize",
                        "-fno-rtti",
                    )
                    cFlags += listOf(
                        "-O3",
//                        "-mfpu=neon-vfpv4",
                        "-ffast-math",
                        "-flto",
                        "-finline-functions",
                        "-ftree-vectorize",
                        "-fno-rtti",
                    )
                }
            }
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
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }
    kotlinOptions {
        jvmTarget = "1.8"
    }
    buildFeatures {
        compose = true
        dataBinding = true
        viewBinding = true
    }
    composeOptions {
        kotlinCompilerExtensionVersion = "1.5.1"
    }
    packaging {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
        }
    }
    externalNativeBuild {
        cmake {
            path = file("src/main/cpp/CMakeLists.txt")
            version = "3.22.1"
        }
    }
}


dependencies {

    implementation("androidx.core:core-ktx:1.12.0")
    implementation("androidx.lifecycle:lifecycle-runtime-ktx:2.6.2")
    implementation("androidx.activity:activity-compose:1.8.2")
    implementation(platform("androidx.compose:compose-bom:2023.08.00"))
    implementation("androidx.compose.ui:ui")
    implementation("androidx.compose.ui:ui-graphics")
    implementation("androidx.compose.ui:ui-tooling-preview")
    implementation("androidx.compose.material3:material3")
    testImplementation("junit:junit:4.13.2")
    androidTestImplementation("androidx.test.ext:junit:1.1.5")
    androidTestImplementation("androidx.test.espresso:espresso-core:3.5.1")
    androidTestImplementation(platform("androidx.compose:compose-bom:2023.08.00"))
    androidTestImplementation("androidx.compose.ui:ui-test-junit4")
    debugImplementation("androidx.compose.ui:ui-tooling")
    debugImplementation("androidx.compose.ui:ui-test-manifest")
    implementation("com.github.amirisback:keyboard:1.1.5")
    implementation( "androidx.compose.runtime:runtime-livedata")
    implementation ("androidx.navigation:navigation-compose:2.7.6")
}