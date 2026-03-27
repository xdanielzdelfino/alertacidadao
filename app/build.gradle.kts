import java.util.Properties
import java.io.FileInputStream

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.jetbrains.kotlin.android)
}

// Load signing properties from project root `keystore.properties` if present
val keystorePropertiesFile = rootProject.file("keystore.properties")
val keystoreProperties = Properties()
if (keystorePropertiesFile.exists()) {
    keystoreProperties.load(FileInputStream(keystorePropertiesFile))
}

android {
    namespace = "com.alertacidadao.app"
    compileSdk = 34

    defaultConfig {
        applicationId = "com.alertacidadao.app"
        minSdk = 24
        targetSdk = 34
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    buildTypes {
        release {
            isMinifyEnabled = true
            // Habilita remoção de recursos não usados para builds de release
            isShrinkResources = true
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
            // Use signing config from keystore.properties when available
            signingConfig = signingConfigs.findByName("release") ?: signingConfigs.create("release") {
                // storeFile, storePassword, keyAlias, keyPassword may be empty if properties missing
                storeFile = file(keystoreProperties.getProperty("storeFile", "alertacidadao.keystore"))
                storePassword = keystoreProperties.getProperty("storePassword", "")
                keyAlias = keystoreProperties.getProperty("keyAlias", "")
                keyPassword = keystoreProperties.getProperty("keyPassword", "")
            }
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
}

dependencies {
    // Bibliotecas core do Android
    implementation(libs.androidx.core.ktx)
    implementation(libs.material)
    implementation(libs.constraint.layout)
    implementation(libs.navigation.fragment)
    implementation(libs.navigation.ui)
    implementation(libs.androidx.core.splashscreen)
    implementation(libs.play.services.location)
    implementation(libs.osmdroid.android)

    // Banco de dados Room
    implementation(libs.room.runtime)
    implementation(libs.room.ktx)
    annotationProcessor(libs.room.compiler)
    
    // Glide (carregamento de imagens)
    implementation(libs.glide)
    annotationProcessor(libs.glide.compiler)

    // Testes
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
}
