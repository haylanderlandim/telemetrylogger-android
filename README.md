# Android Telemetry Logger

Biblioteca Android para envio de logs estruturados em JSON usando OpenTelemetry e Elastic APM.

## Visão Geral

Esta biblioteca permite que aplicativos Android enviem logs estruturados para sistemas de observabilidade compatíveis com OpenTelemetry, incluindo Elastic APM. Os logs são enviados em formato JSON com atributos customizados, facilitando análise e monitoramento.

Suporta:

* Inicialização simples com parâmetros do serviço e endpoint
* Envio de eventos JSON estruturados como logs
* Abstração para múltiplas implementações (OpenTelemetry, Elastic APM)
* Configuração de contexto (nome do serviço, versão, ambiente)
* Uso de singleton para garantir instância única da lib no app

---

## Funcionalidades

* Inicializa clientes OpenTelemetry ou Elastic APM para enviar logs via OTLP/gRPC
* Transforma JSON arbitrário em atributos e corpo de logs OpenTelemetry
* Suporte a atributos aninhados no JSON, convertidos em chaves pontuadas
* Fácil integração em apps Android com métodos simples `initialize` e `logEvent`
* Compatível com Android API nível 21+

---

## Instalação

### Como usar localmente (exemplo)

1. Adicione a biblioteca `.aar` no seu projeto Android (gerar usando Gradle, ver seção Build).

2. Importe no seu módulo:

```kotlin
implementation project(":telemetrylogger")
```

Ou, caso publique em Maven local, adicione:

```gradle
repositories {
    mavenLocal()
}
dependencies {
    implementation("br.com.landim:telemetrylogger:1.0.0")
}
```

---

## Uso

### Permissões Necessárias

Certifique-se de declarar a seguinte permissão no `AndroidManifest.xml` do **aplicativo (app)** que consome esta biblioteca:

```xml
<uses-permission android:name="android.permission.INTERNET" />
```

> ⚠️ Esta permissão **não é adicionada automaticamente pela biblioteca**. O app precisa declará-la para que a comunicação com o APM (via rede) funcione corretamente.

---

### Inicialização

Chame o método `initialize` da implementação desejada (OpenTelemetry ou Elastic APM) **e depois** passe a instância resultante para o `TelemetryLogger.init(...)`.

Recomenda-se fazer isso no `Application` da sua app, ou em algum ponto de entrada central:

```kotlin
val logger = OpenTelemetryLogger.initialize( // ou ElasticApmLogger.initialize
    application = this,
    serviceName = "meu-app",
    serviceVersion = "1.0.0",
    environment = "production",
    apmEndpoint = "https://meu-apm-endpoint:4317",
    apiKey = "seu-api-key"
)

TelemetryLogger.init(logger)
```

---

### Enviando logs estruturados em JSON

Para enviar um evento de log com dados JSON:

```kotlin
val json = """
{
  "message": "Usuário autenticado com sucesso",
  "userId": "1234",
  "success": true,
  "latencyMs": 145.7
}
"""

TelemetryLogger.logEvent(json)
```

---

## Design e Arquitetura

* Usa singleton para manter instância única e evitar múltiplas conexões
* Separação clara entre interface (`TelemetryLogger`) e implementações (`OpenTelemetryLogger`, `ElasticApmLogger`)
* JSON é transformado em atributos do log usando `JsonAttributeParser`
* Suporte para múltiplos backends via polimorfismo e interface comum
* Configura recursos do serviço com atributos padrão de ambiente e dispositivo (Android)