package br.com.landim.telemetrylogger.agents

import android.app.Application
import android.os.Build
import br.com.landim.telemetrylogger.interfaces.TelemetryLogger
import br.com.landim.telemetrylogger.util.JsonAttributeParser
import com.google.gson.JsonParser
import io.opentelemetry.api.OpenTelemetry
import io.opentelemetry.api.common.AttributeKey
import io.opentelemetry.exporter.otlp.logs.OtlpGrpcLogRecordExporter
import io.opentelemetry.exporter.otlp.trace.OtlpGrpcSpanExporter
import io.opentelemetry.sdk.OpenTelemetrySdk
import io.opentelemetry.sdk.logs.SdkLoggerProvider
import io.opentelemetry.sdk.logs.export.BatchLogRecordProcessor
import io.opentelemetry.sdk.resources.Resource
import io.opentelemetry.sdk.trace.SdkTracerProvider
import io.opentelemetry.sdk.trace.export.SimpleSpanProcessor
import io.opentelemetry.semconv.ServiceAttributes
import io.opentelemetry.semconv.TelemetryAttributes
import io.opentelemetry.semconv.incubating.DeploymentIncubatingAttributes
import io.opentelemetry.semconv.incubating.DeviceIncubatingAttributes
import io.opentelemetry.semconv.incubating.OsIncubatingAttributes
import io.opentelemetry.semconv.incubating.ProcessIncubatingAttributes

/**
 * Logger baseado em OpenTelemetry puro para Android.
 * https://github.com/open-telemetry
 * https://github.com/open-telemetry/opentelemetry-android
 * Envia logs estruturados em formato JSON via protocolo OTLP.
 */
object OpenTelemetryLogger : TelemetryLogger {

    // Instância principal do OpenTelemetry
    private lateinit var openTelemetry: OpenTelemetry
    private lateinit var serviceName: String

    /**
     * Inicializa o OpenTelemetry com exportadores para Trace e Log.
     * Deve ser chamado uma única vez no início do app.
     *
     * @param application Contexto da aplicação Android
     * @param serviceName Nome do serviço (ex: "my-app")
     * @param serviceVersion Versão do serviço (ex: "1.0.0")
     * @param environment Ambiente de execução (ex: "production", "staging")
     * @param apmEndpoint URL do collector OTLP (ex: https://my-apm-endpoint:4317)
     * @param apiKey Chave de autenticação para o APM
     */
    override fun initialize(
        application: Application,
        serviceName: String,
        serviceVersion: String,
        environment: String,
        apmEndpoint: String,
        apiKey: String
    ): TelemetryLogger {
        this.serviceName = serviceName

        // Exportador de traces (spans) via OTLP gRPC
        val traceExporter = OtlpGrpcSpanExporter.builder()
            .setEndpoint(apmEndpoint)
            .addHeader("Authorization", "Bearer $apiKey")
            .build()

        // Exportador de logs estruturados via OTLP gRPC
        val logExporter = OtlpGrpcLogRecordExporter.builder()
            .setEndpoint(apmEndpoint)
            .addHeader("Authorization", "Bearer $apiKey")
            .build()

        // Processador de spans (simples, síncrono)
        val spanProcessor = SimpleSpanProcessor.create(traceExporter)

        // Provedor de spans com metadados do app
        val tracerProvider = SdkTracerProvider.builder()
            .addSpanProcessor(spanProcessor)
            .setResource(getResource(serviceName, serviceVersion, environment))
            .build()

        // Provedor de logs com os mesmos metadados
        val logProvider = SdkLoggerProvider.builder()
            .addLogRecordProcessor(BatchLogRecordProcessor.builder(logExporter).build())
            .setResource(getResource(serviceName, serviceVersion, environment))
            .build()

        // Inicializa o OpenTelemetry final
        openTelemetry = OpenTelemetrySdk.builder()
            .setTracerProvider(tracerProvider)
            .setLoggerProvider(logProvider)
            .build()
        return this
    }

    /**
     * Envia log estruturado no formato JSON usando a instância do OpenTelemetry.
     */
    override fun logJsonEvent(json: String) {
        try {
            val jsonObject = JsonParser.parseString(json).asJsonObject
            val logger = openTelemetry.logsBridge[serviceName]
            val logBuilder = logger.logRecordBuilder()
            JsonAttributeParser.parseIntoLog(logBuilder, jsonObject)
            logBuilder.emit()
        } catch (e: Exception) {
            // Log em caso de erro de parsing
            val logger = openTelemetry.logsBridge[serviceName]
            logger.logRecordBuilder()
                .setBody("Erro ao processar JSON para log: ${e.message}")
                .emit()
        }
    }

    /**
     * Cria metadados padrão para enrich dos logs e spans com info do dispositivo/app.
     */
    private fun getResource(
        serviceName: String,
        serviceVersion: String,
        environment: String
    ): Resource {
        return Resource.builder()
            .put(ServiceAttributes.SERVICE_NAME, serviceName)
            .put(ServiceAttributes.SERVICE_VERSION, serviceVersion)
            .put(DeploymentIncubatingAttributes.DEPLOYMENT_ENVIRONMENT_NAME, environment)
            .put(AttributeKey.stringKey("app.installation.id"), "")
            .put(DeviceIncubatingAttributes.DEVICE_MODEL_IDENTIFIER, Build.MODEL)
            .put(DeviceIncubatingAttributes.DEVICE_MANUFACTURER, Build.MANUFACTURER)
            .put(OsIncubatingAttributes.OS_DESCRIPTION, getOsDescription())
            .put(OsIncubatingAttributes.OS_VERSION, Build.VERSION.RELEASE)
            .put(OsIncubatingAttributes.OS_NAME, "Android")
            .put(ProcessIncubatingAttributes.PROCESS_RUNTIME_NAME, "Android Runtime")
            .put(
                ProcessIncubatingAttributes.PROCESS_RUNTIME_VERSION,
                System.getProperty("java.vm.version")
            )
            .put(TelemetryAttributes.TELEMETRY_SDK_NAME, "android")
            .put(TelemetryAttributes.TELEMETRY_SDK_VERSION, "1.0.0")
            .put(TelemetryAttributes.TELEMETRY_SDK_LANGUAGE, "java")
            .build()
    }

    private fun getOsDescription(): String =
        "Android ${Build.VERSION.RELEASE}, API level ${Build.VERSION.SDK_INT}, BUILD ${Build.VERSION.INCREMENTAL}"
}
