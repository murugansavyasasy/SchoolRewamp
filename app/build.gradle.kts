import groovy.json.JsonOutput
import groovy.json.JsonSlurper
import java.io.FileInputStream
import java.io.FileOutputStream
import java.io.FileReader

plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
    id("com.google.gms.google-services")
    id("kotlin-parcelize")
    id("com.google.firebase.crashlytics")
    id("org.jetbrains.kotlin.plugin.compose")
}
android {
    namespace = "com.vs.schoolmessenger"
    compileSdk = 35
    ndkVersion = "28.0.12433566"
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
        versionCode = 205
        versionName = "8.12"
        // 👇 Add these lines
        buildConfigField("int", "VERSION_CODE", versionCode.toString())
        buildConfigField("String", "VERSION_NAME", "\"$versionName\"")
        buildConfigField(
            "String",
            "TERMS_URL",
            "\"https://schoolchimes.com/vs_web/terms_conditions/\""
        )
        buildConfigField("boolean", "BASE_APP", "true")
        buildConfigField("String", "SCHOOL_ID", "\"\"")

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
//        ndk {
//            abiFilters += listOf(
//                "armeabi-v7a", "arm64-v8a", "x86",
//                "x86_64"
//            )
//        }
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
            applicationId = "com.vs.schoolmessenger"
            buildConfigField(
                "String",
                "TERMS_URL",
                "\"https://schoolchimes.com/vs_web/terms_conditions/\""
            )
            buildConfigField("boolean", "BASE_APP", "true")
            buildConfigField("String", "SCHOOL_ID", "\"\"")


        }
        // ✅ 2️⃣ Dynamically Generate Other Flavors
        val schoolsFile = rootDir.resolve("app/schools.json")
        if (schoolsFile.exists()) {
            val jsonSlurper = JsonSlurper()
            val schools = jsonSlurper.parse(FileReader(schoolsFile)) as List<Map<String, Any>>
            schools.forEach { school ->
                val id = school["id"] as String
                val package_name = school["package_suffix"] as String
                val termsUrl = school["terms_url"] as String
                val school_id = school["school_id"] as String

                create(id) {
                    dimension = "school"
                    applicationId = package_name
                    buildConfigField(
                        "String",
                        "TERMS_URL",
                        "\"$termsUrl\""
                    )
                    buildConfigField("boolean", "BASE_APP", "false")
                    buildConfigField("String", "SCHOOL_ID", "\"$school_id\"")
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
                val ID = school["id"].toString()
                val schoolName = school["name"].toString()
                val schoolColor = school["color"].toString()
                val start_color = school["start_color"].toString()
                val center_color = school["center_color"].toString()
                val end_color = school["end_color"].toString()
                val dark_blue_color = school["dark_blue_color"].toString()
                val light_sky_blue_color = school["light_sky_blue_color"].toString()
                val iconName = school["icon"].toString()

                val empowering = school["empowering"].toString()
                val _3000_schools = school["_3000_schools"].toString()
                val welcome_splash_message = school["welcome_splash_message"].toString()
                val welcome_to_school_chimes = school["welcome_to_school_chimes"].toString()


                val flavorResDir = File(srcDir, "$ID/res")
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
                    println("⚠️ Warning: Logo not found for $ID")
                }
                // Create strings.xml
                val stringsXml = File(valuesDir, "strings.xml")
                stringsXml.writeText(
                    """
                |<?xml version="1.0" encoding="utf-8"?>
                |<resources>
                |    <string name="app_name">$schoolName</string>
                |    <string name="empowering">$empowering</string>
                |    <string name="_3000_schools">$_3000_schools</string>
                |    <string name="welcome_splash_message">$welcome_splash_message</string>
                |    <string name="welcome_to_school_chimes">$welcome_to_school_chimes</string>
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
                println("✅ Resources created for $ID")
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
                val ID = school["id"].toString()
                val packageName = school["package_suffix"] as String
                val flavorDir = File("${rootDir}/app/src/$ID/")
                if (!flavorDir.exists()) {
                    flavorDir.mkdirs()
                }
                // Deep copy the JSON template to avoid modifying the original in memory
                val googleServicesCopy =
                    jsonSlurper.parseText(JsonOutput.toJson(googleServicesTemplate)) as Map<String, Any>
                val clientList = googleServicesCopy["client"] as? List<MutableMap<String, Any>>
                if (clientList != null && clientList.isNotEmpty()) {
                    val clientInfo = clientList[0]["client_info"] as? MutableMap<String, Any>
                    val androidClientInfo =
                        clientInfo?.get("android_client_info") as? MutableMap<String, Any>
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
                println("✅ Generated google-services.json for $ID with package $packageName")
            }
        }
    }
}

dependencies {
    coreLibraryDesugaring("com.android.tools:desugar_jdk_libs:2.1.5")
    implementation("androidx.fragment:fragment-ktx:1.8.9")// or latest stable version
    implementation("androidx.activity:activity-compose:1.10.1")
    implementation(platform("androidx.compose:compose-bom:2026.01.01"))
    implementation("androidx.compose.material3:material3")
    testImplementation("junit:junit:4.13.2")
    androidTestImplementation(platform("androidx.compose:compose-bom:2026.01.01"))
    androidTestImplementation("androidx.compose.ui:ui-test-junit4")
    debugImplementation("androidx.compose.ui:ui-tooling")
    debugImplementation("androidx.compose.ui:ui-test-manifest")
    implementation("com.airbnb.android:lottie:6.0.0")
    implementation("com.github.bumptech.glide:glide:5.0.5")
    implementation("de.hdodenhof:circleimageview:3.1.0")
    implementation("com.facebook.shimmer:shimmer:0.5.0")
    implementation("com.squareup.retrofit2:retrofit:3.0.0")
    implementation("com.squareup.retrofit2:converter-gson:3.0.0")
    implementation("com.squareup.okhttp3:logging-interceptor:5.3.2")
    implementation("com.squareup.okhttp3:okhttp:5.3.2")
    //Firebase Message
    implementation("com.google.firebase:firebase-messaging:25.0.1")
    implementation("com.google.firebase:firebase-common-ktx:21.0.0")
    implementation("com.google.firebase:firebase-database:22.0.1")
    implementation("com.google.firebase:firebase-database-ktx:21.0.0")
    //Load Image
    implementation("com.squareup.picasso:picasso:2.71828")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.10.2")
    implementation("androidx.viewpager2:viewpager2:1.1.0")
    //CircleIndicator
    implementation("me.relex:circleindicator:2.1.6")
    implementation("androidx.security:security-crypto:1.1.0") // latest as of now
    implementation("com.google.android.play:app-update-ktx:2.1.0")
    implementation("com.google.android.material:material:1.13.0")
    implementation("androidx.biometric:biometric:1.1.0")
    annotationProcessor("com.github.bumptech.glide:compiler:5.0.5")
    implementation("com.google.android.gms:play-services-location:21.3.0")
    implementation("com.google.android.gms:play-services-auth:21.5.0")
    implementation("com.google.android.gms:play-services-maps:20.0.0")
    implementation(platform("com.google.firebase:firebase-bom:34.9.0"))
    // Crashlytics
    implementation("com.google.firebase:firebase-crashlytics")
    implementation("com.github.chrisbanes:PhotoView:2.3.0")
    implementation("androidx.lifecycle:lifecycle-process:2.10.0")
    implementation("androidx.drawerlayout:drawerlayout:1.2.0")
    implementation("com.google.android.flexbox:flexbox:3.0.0")
    implementation("androidx.core:core-splashscreen:1.2.0")

    implementation("org.maplibre.gl:android-sdk:12.3.1")
    implementation("org.maplibre.gl:android-plugin-annotation-v9:3.0.2")
    implementation("com.google.android.exoplayer:exoplayer:2.19.1")
}