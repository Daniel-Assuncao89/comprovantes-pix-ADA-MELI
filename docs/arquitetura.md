# Arquitetura — Comprovantes PIX (projeto guiado)

Documento vivo: cresce com as aulas. Estado-alvo ao fim do módulo.

## Contextos e fluxo

```
                 POST /comprovantes
                        │  (202 + id)
                        ▼
              ┌───────────────────┐
              │ comprovante-emissor│  bounded context: EMISSÃO
              └─────────┬─────────┘
                        │ publica GravarComprovanteCommand
                        ▼  (FILA — RabbitMQ)             Aula 4/5
              ┌───────────────────┐
              │ comprovante-gravador│ bounded context: GRAVAÇÃO (base própria)
              └─────────┬─────────┘  idempotente (Aula 5)
                        │ publica ComprovanteGravadoEvent
                        ▼  (TÓPICO — Kafka)              Aula 6
        ┌───────────────┼───────────────┐
        ▼               ▼               ▼   consumer groups (Aula 6/7)
   notificação      antifraude          BI
   (@RetryableTopic, Aula 7)

              GET /comprovantes/{id}
                        ▼
              ┌───────────────────┐
              │ comprovante-consulta│ bounded context: CONSULTA
              └───────────────────┘  cache → banco → 3 retries → 404  (Aula 3)
```

## Mapa de incrementos por aula
| Aula | Padrão | Onde aparece |
|---|---|---|
| 1 | DDD / bases segregadas | 3 serviços, H2 por serviço, ADR-001 |
| 2 | SAGA / idempotência | orquestração emissão→gravação |
| 3 | Cache | comprovante-consulta |
| 4 | Producer/consumer | emissor publica, gravador consome |
| 5 | Filas + DLQ | fila de gravação |
| 6 | Tópicos | evento "comprovante-gravado" |
| 7 | Resiliência | `@RetryableTopic` + circuit breaker |
| 8 | Contract test | pacts em shared-contracts |

## Contratos entre serviços
Em `shared-contracts`: `ComprovanteRequest`, `ComprovanteAceito`, `GravarComprovanteCommand` (fila), `ComprovanteGravadoEvent` (tópico). Pact files (Aula 8) em `shared-contracts/src/pacts`.
