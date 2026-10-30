# Comprovantes PIX — Aula 1 (DDD)

Exemplo da Aula 1 do projeto guiado. Uma API que **emite** e **consulta** comprovantes de PIX que já aconteceram.

## As duas branches

| Branch | O que tem |
|---|---|
| `v0` | Só a **estrutura**: os módulos, as classes vazias e os `// TODO`. É o ponto de partida da aula. |
| `gabarito` | Como a aula **deve terminar**: validação no emissor + o modelo de domínio com **entidade** e **Value Objects**. |

Para trocar de uma para a outra:

```bash
git checkout v0
git checkout gabarito
```

Para ver tudo o que muda entre as duas:

```bash
git diff v0 gabarito
```

## Os módulos

Cada serviço é um **bounded context** e tem o seu próprio banco.

| Módulo | Porta | O que faz |
|---|---|---|
| `comprovante-emissor` | 8081 | Recebe o `POST /comprovantes`, valida e responde `202` |
| `comprovante-gravador` | 8082 | Salva o comprovante no banco dele |
| `comprovante-consulta` | 8083 | Responde o `GET /comprovantes/{id}` (por enquanto sempre `404`) |
| `shared-contracts` | — | Classes que os serviços trocam entre si (os "envelopes" de dados) |

```
aula-01-ddd-exemplo/
├── pom.xml                    # projeto "pai", junta os módulos
├── shared-contracts/
├── comprovante-emissor/
├── comprovante-gravador/
├── comprovante-consulta/
└── docs/
    ├── arquitetura.md         # desenho dos serviços e de como eles crescem nas aulas
    └── adr/                   # decisões registradas (ADR-001: cada serviço com seu banco)
```

## Como rodar

Precisa de **Java 21** e **Maven**.

```bash
mvn compile                                     # compila tudo
mvn -pl comprovante-emissor spring-boot:run     # sobe o emissor na porta 8081
```

Testar o emissor (em outro terminal, ou pelo Postman):

```bash
curl -i -X POST localhost:8081/comprovantes -H 'Content-Type: application/json' \
  -d '{"nome":"Giovanni","tipo_documento":"CPF","numero_documento":"50329291076",
       "numero_agencia":"2022","numero_conta":"00276","digito_verificador_conta":"0",
       "valor_transacao":23.99,"tipo_chave_pix_destino":"CELULAR","chave_pix_destino":"11948755536",
       "nome_cliente_destino":"Fernando","identificacao_pix":"churrasco",
       "data_hora_transacao":"2022-04-10T20:03:57"}'
```

- Na `v0`, responde `202` para qualquer coisa (ainda não valida nada).
- No `gabarito`, responde `202` se estiver tudo certo e `400` se algum campo estiver errado (tente `"numero_conta":"002761"`).

## Onde mexer na aula

Procure os comentários `// TODO (Aula 1)`. Os `TODO` das outras aulas ficam para depois.
