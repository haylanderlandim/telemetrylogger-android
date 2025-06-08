package br.com.landim.telemetrylogger.interfaces

import android.app.Application

/**
 * Interface para envio de logs estruturados (em formato JSON ou Map<String, Any>).
 * Permite abstrair a implementação de log (Elastic, OpenTelemetry etc.).
 */
interface TelemetryLogger {

    /**
     * Inicializa o provider de log com os parâmetros da aplicação.
     */
    fun initialize(
        application: Application,
        serviceName: String,
        serviceVersion: String,
        environment: String,
        apmEndpoint: String,
        apiKey: String
    ): TelemetryLogger

    /**
     * Envia um evento de log em formato JSON.
     */
    fun logJsonEvent(json: String)

    companion object {
        private var instance: TelemetryLogger? = null

        /**
         * Inicializa a instância global de TelemetryLogger.
         */
        fun init(logger: TelemetryLogger) {
            instance = logger
        }

        /**
         * Envia log usando a instância global configurada.
         * Lança erro se init() não foi chamado.
         */
        fun logEvent(json: String) {
            instance?.logJsonEvent(json)
                ?: throw IllegalStateException("TelemetryLogger não inicializado")
        }
    }
}

