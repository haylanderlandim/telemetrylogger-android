package br.com.landim.telemetrylogger.util

import com.google.gson.JsonElement
import com.google.gson.JsonObject
import io.opentelemetry.api.common.AttributeKey
import io.opentelemetry.api.logs.LogRecordBuilder
import io.opentelemetry.api.trace.Span

/**
 * Objeto utilitário responsável por converter objetos JSON em atributos
 * para Spans ou Logs do OpenTelemetry.
 *
 * Fornece métodos para transformar pares chave-valor de JSON em atributos
 * correspondentes do OpenTelemetry, suportando objetos JSON aninhados.
 */
object JsonAttributeParser {

    /**
     * Converte o objeto [json] fornecido em atributos do OpenTelemetry
     * aplicados ao [span] informado.
     *
     * Suporta objetos JSON aninhados, adicionando o prefixo das chaves pai
     * separadas por '.' para manter a hierarquia dos atributos.
     *
     * @param span Span do OpenTelemetry onde os atributos serão adicionados.
     * @param json Objeto Json contendo os pares chave-valor para converter.
     * @param prefix Prefixo opcional para as chaves, usado em atributos aninhados.
     */
    fun parseIntoSpan(span: Span, json: JsonObject, prefix: String = "") {
        for ((key, value) in json.entrySet()) {
            // Compor a chave completa com prefixo, caso haja hierarquia
            val fullKey = if (prefix.isEmpty()) key else "$prefix.$key"
            applyAttribute(span, fullKey, value)
        }
    }

    /**
     * Converte o objeto [json] fornecido em atributos do OpenTelemetry
     * aplicados ao [logBuilder] informado.
     *
     * Tratamento especial:
     * - Se a chave for "message" e o valor for string, é definido como corpo do log.
     * - Caso contrário, os atributos são definidos conforme o tipo primitivo JSON:
     *   string, booleano, número ou fallback para representação string.
     *
     * @param logBuilder Construtor de registro de log onde os atributos serão adicionados.
     * @param json Objeto Json contendo os pares chave-valor para converter.
     */
    fun parseIntoLog(logBuilder: LogRecordBuilder, json: JsonObject) {
        for ((key, value) in json.entrySet()) {
            // Caso especial: "message" vira corpo do log se for string
            if (key == "message" && value.isJsonPrimitive && value.asJsonPrimitive.isString) {
                logBuilder.setBody(value.asString)
                continue
            }

            // Define o atributo conforme o tipo do valor JSON
            when {
                value.isJsonPrimitive && value.asJsonPrimitive.isString ->
                    logBuilder.setAttribute(AttributeKey.stringKey(key), value.asString)

                value.isJsonPrimitive && value.asJsonPrimitive.isBoolean ->
                    logBuilder.setAttribute(AttributeKey.booleanKey(key), value.asBoolean)

                value.isJsonPrimitive && value.asJsonPrimitive.isNumber ->
                    logBuilder.setAttribute(AttributeKey.doubleKey(key), value.asDouble)

                else ->
                    // Caso padrão: armazena o elemento JSON como string
                    logBuilder.setAttribute(AttributeKey.stringKey(key), value.toString())
            }
        }
    }

    /**
     * Método auxiliar que aplica um elemento JSON como atributo no [span] fornecido.
     *
     * - Se o elemento for um objeto JSON aninhado, chama recursivamente para manter hierarquia.
     * - Se for primitivo (string, booleano, número), define o atributo diretamente.
     *
     * @param span Span do OpenTelemetry onde o atributo será definido.
     * @param key Nome completo da chave para o atributo.
     * @param value Elemento JSON a ser convertido e aplicado.
     */
    private fun applyAttribute(span: Span, key: String, value: JsonElement) {
        when {
            value.isJsonObject -> parseIntoSpan(span, value.asJsonObject, key)
            value.isJsonPrimitive -> {
                val primitive = value.asJsonPrimitive
                when {
                    primitive.isString -> span.setAttribute(key, primitive.asString)
                    primitive.isBoolean -> span.setAttribute(key, primitive.asBoolean)
                    primitive.isNumber -> span.setAttribute(key, primitive.asNumber.toDouble())
                }
            }
            // Outros tipos JSON (ex: arrays, nulos) não são tratados explicitamente aqui
        }
    }
}