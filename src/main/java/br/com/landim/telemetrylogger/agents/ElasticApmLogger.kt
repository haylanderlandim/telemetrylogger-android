package br.com.landim.telemetrylogger.agents

import android.app.Application
import br.com.landim.telemetrylogger.interfaces.TelemetryLogger
import br.com.landim.telemetrylogger.util.JsonAttributeParser
import co.elastic.otel.android.ElasticApmAgent
import co.elastic.otel.android.connectivity.Authentication
import co.elastic.otel.android.exporters.configuration.ExportProtocol
import com.google.gson.JsonParser

/**
 * Logger baseado no Elastic APM Agent Android.
 * https://www.elastic.co/docs/reference/apm/agents/android/
 * Usa o OpenTelemetry por trás dos panos para exportar logs estruturados.
 */
object ElasticApmLogger : TelemetryLogger {

    // Agente Elastic já encapsula OpenTelemetry
    private lateinit var agent: ElasticApmAgent
    private lateinit var serviceName: String

    /**
     * Inicializa o agente Elastic APM com exportador gRPC.
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

        agent = ElasticApmAgent.builder(application)
            .setServiceName(serviceName)
            .setServiceVersion(serviceVersion)
            .setDeploymentEnvironment(environment)
            .setExportUrl(apmEndpoint)
            .setExportAuthentication(Authentication.SecretToken(apiKey))
            .setExportProtocol(ExportProtocol.GRPC)
            .build()
        return this
    }

    /**
     * Envia log estruturado no formato JSON usando a instância do agente.
     */
    override fun logJsonEvent(json: String) {
        try {
            val jsonObject = JsonParser.parseString(json).asJsonObject
            val logger = agent.getOpenTelemetry().logsBridge[serviceName]
            val logBuilder = logger.logRecordBuilder()
            JsonAttributeParser.parseIntoLog(logBuilder, jsonObject)
            logBuilder.emit()
        } catch (e: Exception) {
            // Log em caso de erro de parsing
            agent.getOpenTelemetry().logsBridge[serviceName]
                .logRecordBuilder()
                .setBody("Erro ao processar JSON para log: ${e.message}")
                .emit()
        }
    }
}