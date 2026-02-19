import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import java.util.Properties

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlinx.serialization)
}

// Helper function to load local.properties
fun getLocalProperties(): Properties {
    val properties = Properties()
    val localPropertiesFile = project.rootProject.file("local.properties")
    if (localPropertiesFile.exists()) {
        localPropertiesFile.inputStream().use { properties.load(it) }
    } else {
        // Handle case where file is missing, can throw an exception or return empty
        println("WARNING: local.properties file not found")
    }
    return properties
}

android {
    namespace = "com.alvinfungai.flower"
    compileSdk {
        version = release(36)
    }

    defaultConfig {
        applicationId = "com.alvinfungai.flower"
        minSdk = 24
        targetSdk = 36
        versionCode = 2
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"

        val properties = getLocalProperties()

        // Get SUPABASE_URL field
        buildConfigField("String", "SUPABASE_URL", "\"${properties.getProperty("SUPABASE_URL")}\"")

        // Get SUPABASE_ANON_KEY field
        buildConfigField("String", "SUPABASE_ANON_KEY", "\"${properties.getProperty("SUPABASE_ANON_KEY")}\"")

        // Google Client ID
        buildConfigField("String", "GOOGLE_CLIENT_ID", "\"${properties.getProperty("GOOGLE_CLIENT_ID")}\"")

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
    kotlin {
        compilerOptions {
            jvmTarget.set(JvmTarget.JVM_11)
        }
    }
    buildFeatures {
        buildConfig = true
    }

    testOptions {
        unitTests.isReturnDefaultValues = true
    }
}

dependencies {
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.appcompat)
    implementation(libs.material)
    implementation(libs.androidx.activity)
    implementation(libs.androidx.constraintlayout)
    implementation(libs.androidx.recyclerview)
    implementation(libs.androidx.navigation.fragment.ktx)
    implementation(libs.androidx.navigation.ui.ktx)
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)

    implementation(libs.ktor.client.core)
    implementation(libs.ktor.client.okhttp)
    implementation(platform(libs.bom))
    implementation(libs.postgrest.kt)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.serialization.json)
    implementation(libs.androidx.recyclerview)
    implementation(libs.coil.kt)
    implementation(libs.coil.network.okhttp)
    implementation(libs.circleimageview)
    implementation(libs.core.splashscreen)

    // Jetpack Navigation for Fragments (View-based)
    implementation(libs.navigation.fragment.ktx)
    implementation(libs.navigation.ui.ktx)

    // For mocking Supabase/Repository
    testImplementation(libs.mockk)

    // For testing StateFlow
    testImplementation(libs.kotlinx.coroutines.test)

    // to test Flow streams
    testImplementation(libs.turbine)

    // Timber logger
    implementation(libs.timber)

    testImplementation(libs.slf4j.simple)

    // Social login: Google
    implementation(libs.androidx.credentials)
    implementation(libs.androidx.credentials.play.services.auth)
    implementation(libs.googleid)

}