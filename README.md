# Telemetry Logger - Android

Android library for sending structured JSON logs using OpenTelemetry and Elastic APM.

## Overview

This library allows Android applications to send structured logs to observability systems compatible with OpenTelemetry, including Elastic APM. Logs are sent in JSON format with custom attributes, enabling easier analysis and monitoring.

Supports:

* Simple initialization with service parameters and endpoint
* Sending structured JSON events as logs
* Abstraction for multiple implementations (OpenTelemetry, Elastic APM)
* Context configuration (service name, version, environment)
* Singleton usage to ensure a single instance of the logger within the app

---

## Features

* Initializes OpenTelemetry or Elastic APM clients to send logs via OTLP/gRPC
* Transforms arbitrary JSON into OpenTelemetry log attributes and body
* Supports nested JSON attributes, converted into dotted keys
* Easy integration into Android apps using simple `initialize` and `logEvent` methods
* Compatible with Android API level 21+

---

## Installation

### Local Usage Example

1. Add the `.aar` library to your Android project (generated via Gradle, see Build section).

2. Import it in your module:

```kotlin
implementation project(":telemetrylogger")
```

Or, if published to the local Maven repository, add:

```gradle
repositories {
    mavenLocal()
}
dependencies {
    implementation("br.com.landim:telemetrylogger:1.0.0")
}
```

---

## Usage

### Required Permissions

Make sure to declare the following permission in the `AndroidManifest.xml` of the **application (app)** using this library:

```xml
<uses-permission android:name="android.permission.INTERNET" />
```

> ⚠️ This permission is **not automatically added by the library**. The app must declare it for proper network communication with the APM server.

---

### Initialization

Call the `initialize` method from the desired implementation (OpenTelemetry or Elastic APM) **and then** pass the resulting instance to `TelemetryLogger.init(...)`.

It is recommended to do this in your app's `Application` class or in a central entry point:

```kotlin
val logger = OpenTelemetryLogger.initialize( // or ElasticApmLogger.initialize
    application = this,
    serviceName = "my-app",
    serviceVersion = "1.0.0",
    environment = "production",
    apmEndpoint = "https://my-apm-endpoint:4317",
    apiKey = "your-api-key"
)

TelemetryLogger.init(logger)
```

---

### Sending Structured JSON Logs

To send a log event with JSON data:

```kotlin
val json = """
{
  "message": "User successfully authenticated",
  "userId": "1234",
  "success": true,
  "latencyMs": 145.7
}
"""

TelemetryLogger.logEvent(json)
```

---

## Design & Architecture

* Uses a singleton to maintain a single instance and avoid multiple connections
* Clear separation between the interface (`TelemetryLogger`) and implementations (`OpenTelemetryLogger`, `ElasticApmLogger`)
* JSON is transformed into log attributes using `JsonAttributeParser`
* Supports multiple backends through polymorphism and a common interface
* Configures service resources with default environment and Android device attributes
