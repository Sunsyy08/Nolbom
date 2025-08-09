plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)
}

android {
    namespace = "com.project.nolbom"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.project.nolbom"
        minSdk = 26
        targetSdk = 35
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"

        // 카카오맵 REST api
        buildConfigField(
            "String",
            "KAKAO_REST_API_KEY",
            "\"${project.property("KAKAO_REST_API_KEY") as String}\""
        )
        // 카카오 네이티브 앱 키도 추가 (지도용)
        buildConfigField(
            "String",
            "KAKAO_NATIVE_APP_KEY",
            "\"${project.property("KAKAO_NATIVE_APP_KEY") as String}\""
        )
        // strings.xml에 API 키 자동 추가
        resValue("string", "kakao_native_app_key", project.property("KAKAO_NATIVE_APP_KEY") as String)
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
    kotlinOptions {
        jvmTarget = "11"
    }
    buildFeatures {
        compose = true
        buildConfig = true
    }
}

dependencies {


    implementation("com.kakao.maps.open:android:2.9.5")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-play-services:1.7.3")
    // Socket.IO 클라이언트 (⭐ 새로 추가)
    implementation("io.socket:socket.io-client:2.0.1")
    implementation("androidx.compose.ui:ui:1.5.4")
    implementation("androidx.compose.material3:material3:1.1.2")
    implementation("androidx.activity:activity-compose:1.8.0")
    // 위치 서비스 (기존과 동일)
    implementation("com.google.android.gms:play-services-location:21.0.1")
    // 권한 처리
    implementation("com.google.accompanist:accompanist-permissions:0.32.0")
    // build.gradle.kts에 추가
    implementation("io.coil-kt:coil-compose:2.4.0")
    implementation("androidx.lifecycle:lifecycle-viewmodel-compose:2.7.0")

    implementation("androidx.lifecycle:lifecycle-viewmodel-compose:2.7.0")
    implementation("com.squareup.retrofit2:converter-gson:2.11.0")
    implementation("com.squareup.retrofit2:converter-gson:2.9.0")
    implementation("com.kakao.sdk:v2-all:2.21.4")
    // Moshi core
    implementation("com.squareup.moshi:moshi:1.15.0")
    // Moshi Kotlin reflection adapter
    implementation("com.squareup.moshi:moshi-kotlin:1.15.0")
    // Retrofit + Moshi (Converter) + OkHttp Logging + Coroutines
    implementation("com.squareup.retrofit2:retrofit:2.9.0")
    implementation("com.squareup.retrofit2:converter-moshi:2.9.0")    // Moshi 대신 Gson 쓰실 땐 converter-gson 사용
    implementation("com.squareup.okhttp3:logging-interceptor:4.9.0")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.6.0")
    implementation("androidx.compose.material:material-icons-extended:1.6.0")
    implementation("androidx.navigation:navigation-compose:2.7.5")
    implementation("com.google.accompanist:accompanist-flowlayout:0.36.0")
    implementation("com.google.android.gms:play-services-maps:18.1.0")
    implementation("com.google.maps.android:maps-compose:2.11.3")
    implementation(libs.gson)
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.activity.compose)
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.ui)
    implementation(libs.androidx.ui.graphics)
    implementation(libs.androidx.ui.tooling.preview)
    implementation(libs.androidx.material3)
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.ui.test.junit4)
    debugImplementation(libs.androidx.ui.tooling)
    debugImplementation(libs.androidx.ui.test.manifest)
}
