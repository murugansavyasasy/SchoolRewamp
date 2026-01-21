import groovy.json.JsonOutput
import groovy.json.JsonSlurper
import java.io.FileInputStream
import java.io.FileOutputStream
import java.io.FileReader


plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
    id("com.google.gms.google-services")
    id ("kotlin-parcelize")
    id("com.google.firebase.crashlytics")
    id("org.jetbrains.kotlin.plugin.compose")
}

android {
    namespace = "com.vs.schoolmessenger"
    compileSdk = 35
    ndkVersion = "28.0.12433566"

//    android {
//        ndkVersion = "26.1.10909125"
//    }

    packaging {
        jniLibs {
            useLegacyPackaging = false
        }
    }

    defaultConfig {

        applicationId = "com.vs.schoolmessenger"
        minSdk = 24
        //noinspection EditedTargetSdkVersion
        targetSdk = 35
        versionCode = 1
        versionName = "1.0"
        // 👇 Add these lines
        buildConfigField("int", "VERSION_CODE", versionCode.toString())
        buildConfigField("String", "VERSION_NAME", "\"$versionName\"")
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        vectorDrawables {
            useSupportLibrary = true
        }
    }
    buildTypes {
        release {
            isMinifyEnabled = false
            isShrinkResources = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }


    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
        isCoreLibraryDesugaringEnabled = true
    }

//    compileOptions {
//        sourceCompatibility = JavaVersion.VERSION_1_8
//        targetCompatibility = JavaVersion.VERSION_1_8
//    }
    kotlinOptions {
        jvmTarget = "1.8"
    }
    buildFeatures {
        compose = true
        viewBinding = true
        buildConfig = true

    }



    packagingOptions {
        jniLibs {
            // Make sure new packaging is used so libs can be aligned properly
            useLegacyPackaging = false
        }
    }
    packaging {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
        }
    }
    flavorDimensions += "school"

    productFlavors {
        create("defaultFlavor") {
            dimension = "school"
//            applicationIdSuffix = "" // No suffix for the main app
            applicationId = "com.vs.schoolmessenger"

        }

        // ✅ 2️⃣ Dynamically Generate Other Flavors
        val schoolsFile = rootDir.resolve("app/schools.json")
        if (schoolsFile.exists()) {
            val jsonSlurper = JsonSlurper()
            val schools = jsonSlurper.parse(FileReader(schoolsFile)) as List<Map<String, Any>>

            schools.forEach { school ->
                val id = school["id"] as String
                val package_name = school["package_suffix"] as String

                create(id) {
                    dimension = "school"
//                    applicationIdSuffix = suffix
                    applicationId = package_name

                }
            }
        } else {
            println("⚠️ Warning: schools.json file not found!")
        }
    }


    tasks.register("generateFlavorResources") {
        doLast {
            val schoolsFile = file("${rootDir}/app/schools.json")
            if (!schoolsFile.exists()) {
                println("⚠️ Warning: schools.json file not found!")
                return@doLast
            }

            val jsonSlurper = JsonSlurper()
            val schools = jsonSlurper.parse(schoolsFile) as List<Map<String, Any>>

            val srcDir = file("${projectDir}/src")

            schools.forEach { school ->
                val schoolId = school["id"].toString()
                val schoolName = school["name"].toString()
                val schoolColor = school["color"].toString()
                val start_color = school["start_color"].toString()
                val center_color = school["center_color"].toString()
                val end_color = school["end_color"].toString()
                val dark_blue_color = school["dark_blue_color"].toString()
                val light_sky_blue_color = school["light_sky_blue_color"].toString()
                val iconName = school["icon"].toString()


                val flavorResDir = File(srcDir, "$schoolId/res")
                val drawableDir = File(flavorResDir, "drawable")
                val valuesDir = File(flavorResDir, "values")
                val layoutDir = File(flavorResDir, "layout")
                val mipmapDir = File(flavorResDir, "mipmap")

                drawableDir.mkdirs()
                valuesDir.mkdirs()
                layoutDir.mkdirs()
                mipmapDir.mkdirs()
                // Copy logo to drawable folder
                val logoFile = file("${rootDir}/logos/$iconName.png")
                if (logoFile.exists()) {
                    val destFile = File(drawableDir, "school_splash_logo.png")
                    FileInputStream(logoFile).use { input ->
                        FileOutputStream(destFile).use { output ->
                            input.copyTo(output)
                        }
                    }
                } else {
                    println("⚠️ Warning: Logo not found for $schoolId")
                }

                // Create strings.xml
                val stringsXml = File(valuesDir, "strings.xml")
                stringsXml.writeText(
                    """
                |<?xml version="1.0" encoding="utf-8"?>
                |<resources>
                |    <string name="app_name">$schoolName</string>
                |</resources>
                """.trimMargin()
                )

                // Create colors.xml
                val colorsXml = File(valuesDir, "colors.xml")
                colorsXml.writeText(
                    """
                |<?xml version="1.0" encoding="utf-8"?>
                |<resources>
              
                |    <color name="PrimaryColor">$schoolColor</color>
                |    <color name="splash_start">$start_color</color>
                |    <color name="splash_center">$center_color</color>
                |    <color name="splash_end">$end_color</color>
                |    <color name="light_sky_blue_color">$light_sky_blue_color</color>
                |    <color name="dark_blue_color">$dark_blue_color</color>
                |</resources>
                """.trimMargin()
                )

                // Create layout XML


                println("✅ Resources created for $schoolId")
            }
        }
    }


    tasks.register("generateGoogleServicesJson") {
        doLast {
            val schoolsFile = file("${rootDir}/app/schools.json")
            val googleServicesTemplateFile = file("${rootDir}/app/google-services.json")

            if (!schoolsFile.exists()) {
                throw GradleException("Error: schools.json file not found!")
            }
            if (!googleServicesTemplateFile.exists()) {
                throw GradleException("Error: google-services.json template not found!")
            }

            val jsonSlurper = JsonSlurper()
            val schools = jsonSlurper.parse(schoolsFile) as List<Map<String, Any>>
            val googleServicesTemplate = jsonSlurper.parse(googleServicesTemplateFile) as Map<*, *>

            schools.forEach { school ->
                val schoolId = school["id"].toString()
                val packageName = school["package_suffix"] as String
//                val packageName = "com.vs.schoolmessenger.$schoolId"
                val flavorDir = File("${rootDir}/app/src/$schoolId/")

                if (!flavorDir.exists()) {
                    flavorDir.mkdirs()
                }

                // Deep copy the JSON template to avoid modifying the original in memory
                val googleServicesCopy = jsonSlurper.parseText(JsonOutput.toJson(googleServicesTemplate)) as Map<String, Any>
                val clientList = googleServicesCopy["client"] as? List<MutableMap<String, Any>>

                if (clientList != null && clientList.isNotEmpty()) {
                    val clientInfo = clientList[0]["client_info"] as? MutableMap<String, Any>
                    val androidClientInfo = clientInfo?.get("android_client_info") as? MutableMap<String, Any>
                    if (androidClientInfo != null) {
                        androidClientInfo["package_name"] = packageName
                    } else {
                        throw GradleException("Error: Missing android_client_info key in google-services.json")
                    }
                } else {
                    throw GradleException("Error: google-services.json is missing 'client' key")
                }

                val outputFile = File(flavorDir, "google-services.json")
                outputFile.writeText(JsonOutput.prettyPrint(JsonOutput.toJson(googleServicesCopy)))
                println("✅ Generated google-services.json for $schoolId with package $packageName")
            }
        }
    }
}

dependencies {
    coreLibraryDesugaring("com.android.tools:desugar_jdk_libs:2.0.4")
    implementation("androidx.fragment:fragment-ktx:1.8.5")// or latest stable version
    implementation("androidx.constraintlayout:constraintlayout:2.2.1")
    implementation("androidx.appcompat:appcompat:1.7.0")
    implementation("com.google.android.material:material:1.12.0")
    implementation("androidx.core:core-ktx:1.15.0")
    implementation("androidx.lifecycle:lifecycle-runtime-ktx:2.8.7")
    implementation("androidx.activity:activity-compose:1.10.1")
    implementation(platform("androidx.compose:compose-bom:2025.03.01"))
    implementation("androidx.compose.ui:ui")
    implementation("androidx.compose.ui:ui-graphics")
    implementation("androidx.compose.ui:ui-tooling-preview")
    implementation("androidx.compose.material3:material3")
    implementation("androidx.camera:camera-lifecycle:1.4.2")
    implementation("com.google.mlkit:vision-common:17.3.0")
    implementation("com.google.android.gms:play-services-mlkit-face-detection:17.1.0")
    implementation("androidx.palette:palette-ktx:1.0.0")
    implementation("androidx.activity:activity:1.10.1")
    implementation("androidx.compose.ui:ui-graphics:1.10.0")
    implementation("androidx.compose.foundation:foundation:1.10.0")
    testImplementation("junit:junit:4.13.2")
    androidTestImplementation("androidx.test.ext:junit:1.2.1")
    androidTestImplementation("androidx.test.espresso:espresso-core:3.6.1")
    androidTestImplementation(platform("androidx.compose:compose-bom:2025.03.01"))
    androidTestImplementation("androidx.compose.ui:ui-test-junit4")
    debugImplementation("androidx.compose.ui:ui-tooling")
    debugImplementation("androidx.compose.ui:ui-test-manifest")
    implementation("androidx.viewpager2:viewpager2:1.1.0")
    implementation("com.makeramen:roundedimageview:2.3.0")
    implementation("com.airbnb.android:lottie:6.0.0")
    implementation("com.github.bumptech.glide:glide:4.16.0")
    implementation("de.hdodenhof:circleimageview:3.1.0")
    implementation("com.facebook.shimmer:shimmer:0.5.0")
    implementation("com.squareup.retrofit2:retrofit:2.11.0")
    implementation("com.squareup.retrofit2:converter-gson:2.11.0")
    implementation("com.squareup.okhttp3:logging-interceptor:4.12.0")
    implementation("com.squareup.okhttp3:okhttp:4.12.0")
    //AWS s3 libraries
    implementation("com.amazonaws:aws-android-sdk-s3:2.59.0")
    implementation("com.amazonaws:aws-android-sdk-cognito:2.20.1")
    implementation("com.amazonaws:aws-android-sdk-cognitoidentityprovider:2.25.0")
    implementation("org.mindrot:jbcrypt:0.4")
    //Firebase Message
    implementation("com.google.firebase:firebase-messaging:24.1.1")
    implementation("com.google.firebase:firebase-common-ktx:21.0.0")
    implementation("com.google.firebase:firebase-database:21.0.0")
    implementation("com.google.firebase:firebase-database-ktx:21.0.0")
    //Load Image
    implementation("com.squareup.picasso:picasso:2.71828")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.7.3")
    implementation("com.github.massoudss:waveformSeekBar:5.0.2")
    // Amplitude will allow you to call setSampleFrom() with files, URLs, Uri and resources
    // Important: Only works with api level 21 and higher
//    implementation("com.github.lincollincol:amplituda:2.2.2") // or newer version
    //Viewpager Implementation
    implementation("androidx.viewpager2:viewpager2:1.1.0")
    //CircleIndicator
    implementation("me.relex:circleindicator:2.1.6")
    implementation("androidx.security:security-crypto:1.1.0-alpha06") // latest as of now
    implementation("com.github.lecho:hellocharts-library:1.5.8@aar")
    implementation("com.google.android.play:app-update:2.1.0")
    implementation("com.google.android.play:app-update-ktx:2.1.0")
    implementation("com.google.android.material:material:1.12.0")
    implementation("androidx.compose.foundation:foundation:1.7.8")
    implementation("androidx.biometric:biometric:1.1.0")
    annotationProcessor ("com.github.bumptech.glide:compiler:4.15.1")
//    implementation("com.google.android.gms:play-services-location:21.3.0")
    implementation("com.google.android.gms:play-services-location:21.0.1")
    implementation("com.google.android.gms:play-services-auth:20.0.0")
//    implementation ("com.google.android.play:core:1.10.3")
    implementation ("com.google.android.play:review-ktx:2.0.1")
    implementation ("com.google.android.gms:play-services-maps:18.2.0")
    implementation ("io.socket:socket.io-client:2.1.0") // stable version
    implementation ("com.github.PhilJay:MPAndroidChart:v3.1.0")


    // Firebase BOM (manages all Firebase versions)
    implementation(platform("com.google.firebase:firebase-bom:32.7.3"))
    // Crashlytics
    implementation("com.google.firebase:firebase-crashlytics")

    implementation("com.github.chrisbanes:PhotoView:2.3.0")
    implementation("androidx.lifecycle:lifecycle-process:2.7.0")

    implementation("androidx.drawerlayout:drawerlayout:1.2.0")
    implementation ("com.google.android.play:review-ktx:2.0.1")

    implementation("com.kizitonwose.calendar:view:2.5.0")

    implementation("com.google.android.flexbox:flexbox:3.0.0")

    implementation ("androidx.core:core-splashscreen:1.0.1")
    implementation("com.getkeepsafe.taptargetview:taptargetview:1.15.0")


// or a newer version

//    // Smallest version (no audio/video codecs)
//    implementation("com.arthenica:ffmpeg-kit-min:4.5.LTS")
//// Full version with video codecs
//    implementation("com.arthenica:ffmpeg-kit-full:4.5.LTS")
//// With HTTPS and extended support
//    implementation("com.arthenica:ffmpeg-kit-full-gpl:4.5.LTS")
}