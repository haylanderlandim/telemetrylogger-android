plugins {
    alias(libs.plugins.android.library)
    alias(libs.plugins.kotlin.android)
}

android {
    namespace = "br.com.landim.telemetrylogger"
    compileSdk = 35

    defaultConfig {
        minSdk = 30

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        consumerProguardFiles("consumer-rules.pro")
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
}

dependencies {

    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.appcompat)
    implementation(libs.material)
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)

    // GSON
    implementation(libs.gson)

    // APM Elastic
    implementation("co.elastic.otel.android:agent-sdk:1.0.0")
    implementation("co.elastic.otel.android:agent-common:1.0.0")
    implementation("co.elastic.otel.android:agent-api:1.0.0")
    implementation("co.elastic.otel.android:api:1.0.0")

    // Open Telemetry
    implementation(libs.opentelemetry.api)
    implementation(libs.opentelemetry.sdk)
    implementation(libs.opentelemetry.exporter.otlp)
    implementation("io.opentelemetry.android:session:0.11.0-alpha")
    implementation("io.opentelemetry.android:android-agent:0.11.0-alpha")
    implementation("io.opentelemetry.instrumentation:opentelemetry-okhttp-3.0:2.16.0-alpha")
    implementation("io.opentelemetry.semconv:opentelemetry-semconv-incubating:1.32.0-alpha")
    implementation("io.opentelemetry.android.instrumentation:android-instrumentation:0.11.0-alpha")
    implementation("io.opentelemetry.instrumentation:opentelemetry-instrumentation-api-incubator:2.16.0-alpha")

}