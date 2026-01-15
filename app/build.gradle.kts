import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import java.util.Properties

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
}

// Helper function to load local.properties
fun getLocalProperties(): Properties {
    val properties = Properties()
    val localPropertiesFile = project.rootProject.file("local.properties")
    if (localPropertiesFile.exists()) {
        localPropertiesFile.inputStream().use { properties.load(it) }
    } else {
        // Optional: Handle case where file is missing (e.g., throw an exception or return empty)
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
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"

        val properties = getLocalProperties()

        // Get SUPABASE_URL field
        buildConfigField("String", "SUPABASE_URL", "\"${properties.getProperty("SUPABASE_URL")}\"")

        // Get SUPABASE_ANON_KEY field
        buildConfigField("String", "SUPABASE_ANON_KEY", "\"${properties.getProperty("SUPABASE_ANON_KEY")}\"")

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
}

dependencies {
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.appcompat)
    implementation(libs.material)
    implementation(libs.androidx.activity)
    implementation(libs.androidx.constraintlayout)
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)

    implementation(libs.ktor.client.core)
    implementation(libs.ktor.client.okhttp)
    implementation(platform(libs.bom))
    implementation(libs.postgrest.kt)
    implementation(libs.androidx.lifecycle.runtime.ktx)
}