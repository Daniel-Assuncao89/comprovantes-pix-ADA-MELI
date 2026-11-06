# Comprovantes PIX — Aulas 1 (DDD), 2 (SAGA) e 3 (comunicação)

Exemplo do projeto guiado. Uma API que **emite** e **consulta** comprovantes de PIX que já aconteceram.

## As branches

| Branch | O que tem |
|---|---|
| `as-is` | O "antes" da Aula 1: um monólito ruim de propósito, para a turma criticar. |
| `v0` | Só a **estrutura**: os módulos, as classes vazias e os `// TODO`. É o ponto de partida da Aula 1. |
| `gabarito` | Fim da Aula 1: validação no emissor + o modelo de domínio com **entidade** e **Value Objects**. |
| `aula-02` | Fim da Aula 2: o emissor chama o gravador numa **SAGA** e sabe desfazer quando a gravação falha. |
| `aula-03` | Fim da Aula 3: no fim da SAGA, o emissor **avisa** com um evento, e SMS, antifraude e BI reagem cada um por conta própria. |

Para trocar de uma para a outra:

```bash
git checkout aula-02
git checkout aula-03
```

Para ver tudo o que muda entre duas branches:

```bash
git diff aula-02 aula-03
```

## Os módulos

Cada serviço é um **bounded context** e tem o seu próprio banco.

| Módulo | Porta | O que faz |
|---|---|---|
| `comprovante-emissor` | 8081 | Recebe o `POST /comprovantes`, valida, chama o gravador e responde `202` (ou `503` se a gravação falhou) |
| `comprovante-gravador` | 8082 | Recebe o `POST /interno/comprovantes` e salva no banco dele, sem gravar duas vezes o mesmo id |
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

**1. Instalar uma vez** (e de novo sempre que trocar de branch). Sem isso, o `spring-boot:run` abaixo não acha o `shared-contracts`:

```bash
mvn install -DskipTests
```

**2. Subir os dois serviços**, cada um no seu terminal. O gravador sobe primeiro:

```bash
mvn -pl comprovante-gravador spring-boot:run    # porta 8082
mvn -pl comprovante-emissor spring-boot:run     # porta 8081
```

**3. Mandar um comprovante** (em outro terminal, ou pelo Postman):

```bash
curl -i -X POST localhost:8081/comprovantes -H 'Content-Type: application/json' \
  -d '{"nome":"Giovanni","tipo_documento":"CPF","numero_documento":"50329291076",
       "numero_agencia":"2022","numero_conta":"00276","digito_verificador_conta":"0",
       "valor_transacao":23.99,"tipo_chave_pix_destino":"CELULAR","chave_pix_destino":"11948755536",
       "nome_cliente_destino":"Fernando","identificacao_pix":"churrasco",
       "data_hora_transacao":"2022-04-10T20:03:57"}'
```

| Situação | Resposta do emissor |
|---|---|
| Tudo certo | `202`, e o comprovante fica `GRAVADO` |
| Campo errado (tente `"numero_conta":"002761"`) | `400`, com o campo errado na `message`, e ninguém chama o gravador |
| Gravador desligado (`Ctrl+C` nele) | `503`, com o id do comprovante na `message`, e o comprovante fica `INVALIDADO` (a SAGA desfez) |

No PowerShell, o `curl` é outro comando. Use o **Git Bash** ou o Postman.

## As demos da Aula 2

**Gravador lento** ("demorar não quer dizer não"). Suba o gravador com um atraso maior que o tempo de espera do emissor (2 segundos):

```bash
mvn -pl comprovante-gravador spring-boot:run -Dspring-boot.run.arguments=--demo.atraso-ms=3000
```

O emissor desiste, responde `503` e marca o comprovante como `INVALIDADO`... mas o gravador **salvou**. Para ver, abra os **dois consoles do H2** (cada serviço tem o seu banco):

| | Emissor | Gravador |
|---|---|---|
| Console | `http://localhost:8081/h2-console` | `http://localhost:8082/h2-console` |
| JDBC URL | `jdbc:h2:mem:emissor` | `jdbc:h2:mem:gravador` |
| O que aparece | o comprovante `INVALIDADO`, com o motivo | o mesmo id, **gravado** |

Nos dois: usuário `sa`, senha em branco, `SELECT * FROM COMPROVANTE_ENTITY;`. Os bancos são em memória: reiniciar o serviço apaga tudo.

**Gravador idempotente.** Mande o mesmo comprovante direto para o gravador duas vezes. A primeira responde `201` (gravei agora), a segunda `200` (já tinha):

```bash
curl -i -X POST localhost:8082/interno/comprovantes -H 'Content-Type: application/json' \
  -d '{"identificador_comprovante":"11111111-1111-1111-1111-111111111111","dados":{"nome":"Giovanni",
       "tipo_documento":"CPF","numero_documento":"50329291076","numero_agencia":"2022",
       "numero_conta":"00276","digito_verificador_conta":"0","valor_transacao":23.99,
       "tipo_chave_pix_destino":"CELULAR","chave_pix_destino":"11948755536",
       "nome_cliente_destino":"Fernando","identificacao_pix":"churrasco",
       "data_hora_transacao":"2022-04-10T20:03:57"}}'
```

## A demo da Aula 3: o SMS fora do ar

Suba o emissor com a "operadora de SMS" fora:

```bash
mvn -pl comprovante-emissor spring-boot:run -Dspring-boot.run.arguments=--demo.sms-fora=true
```

- **Como está na branch (com `@Async`):** o `POST` responde `202`. O erro do SMS aparece só no log, numa thread `task-N`. O PIX não cai junto.
- **Tire o `@Async` da `NotificacaoSms` e suba de novo:** o `POST` responde `500` ("operadora de SMS fora do ar")... mas o comprovante ficou `GRAVADO` nos dois bancos. Evento **não** quer dizer assíncrono: são duas decisões diferentes.

## O que mudou nesta branch (`aula-03`)

**Emissor**: o núcleo do dinheiro continua **orquestrado** (a SAGA da Aula 2). A periferia passa a ser **coreografada**: quando a SAGA termina, o emissor publica um evento, e quem se interessa reage.

| Onde | O que entrou |
|---|---|
| `dominio/ComprovanteGravado` | O **evento**: "o comprovante foi gravado". Leva o id e o valor. |
| `EmissaoService` | No fim da SAGA, publica o `ComprovanteGravado` (`ApplicationEventPublisher`). Não sabe quem escuta. |
| `reacoes/NotificacaoSms`, `reacoes/Antifraude`, `reacoes/PainelBi` | As reações (aqui, só um log). Cada uma com `@EventListener` + `@Async`: roda na sua thread, e a falha de uma não derruba o PIX. |
| `EmissorApplication` | `@EnableAsync`, que liga o `@Async`. |
| `application.yml` | `demo.sms-fora` (só para a demo). |

⚠️ As reações moram no emissor **só por enquanto**: sem um broker, o evento não sai do processo. Se o emissor cair entre o `GRAVADO` e o SMS, o SMS se perde. Quem resolve isso são as aulas de fila e de Kafka.

**Testes** (`mvn test`, 17 no total):
- `EventoComprovanteGravadoTest`: a SAGA terminou → **um** evento, com o valor. A SAGA compensou → **nenhum** evento.
- `SmsForaTest`: com o SMS fora, o PIX continua `GRAVADO`. Quebra se alguém tirar o `@Async`.

## O exercício da Aula 3: o cashback

Está em [`EXERCICIO-AULA-03.md`](EXERCICIO-AULA-03.md). O negócio pediu cashback em todo PIX, e a dupla coloca o cashback no emissor em 6 passos curtos. Em cada passo, roda o fluxo inteiro, olha o log, a resposta ou o banco, e responde perguntas sobre as palavras da aula. O Java é pouco e vem pronto para copiar.

O desafio da `Idempotency-Key` (que ficou da Aula 2) é o desafio extra do exercício.

## O que mudou na branch `aula-02`

**Emissor**: virou o **orquestrador** de uma SAGA de dois passos.

1. Cria o `Comprovante` e guarda como `ACEITO`.
2. Chama o gravador.
3. Deu certo → marca `GRAVADO` e responde `202`.
4. Deu errado → **compensa**: marca `INVALIDADO` (com o motivo, sem apagar) e responde `503`.

| Onde | O que entrou |
|---|---|
| `dominio/StatusComprovante` | `ACEITO`, `GRAVADO`, `INVALIDADO` |
| `dominio/Comprovante` | `confirmarGravacao()` e `invalidar(motivo)`, a compensação. Invalidar duas vezes não muda nada. `reconstituir(...)` monta de volta o que veio do banco. |
| `persistencia/ComprovanteEntity` | A linha da tabela (H2, `jdbc:h2:mem:emissor`). Separada do `Comprovante`, para o domínio continuar sem anotação de banco. |
| `persistencia/ComprovanteRepository` | A base do emissor na língua do domínio. Cada `salvar()` é uma transação local, com o seu `COMMIT`. |
| `gravacao/client/GravadorClient` | A chamada HTTP para o gravador, com tempo máximo de espera (`gravador.timeout-ms`) |
| `gravacao/GravacaoAdapter` | Esconde o HTTP do resto do código. Na aula de fila, só ele muda. |
| `gravacao/GravacaoFalhouException` | Vira `503` |
| `EmissaoService` | Os passos da SAGA, com o `try/catch` da compensação. **Sem `@Transactional`, de propósito**: o rollback apagaria o `INVALIDADO`. |

**Gravador**: ganhou a "porta da frente", o `GravadorController` (`POST /interno/comprovantes`). Se o id já existe, não grava de novo e responde `200`. Também passou a ler o JSON em `snake_case`, igual ao emissor.

**Consulta**: não muda.

**Testes** (`mvn test`, 14 no total):
- `EmissaoSagaTest`: o caminho feliz e a compensação, com a base de verdade do emissor e o gravador de mentira (Mockito). Confere o que ficou no banco.
- `ComprovanteControllerTest`: `202`, `400` e agora `503`.
- `DominioTest`: os de antes, mais "invalidar duas vezes dá no mesmo" e "comprovante invalidado não pode ser confirmado".
- `GravadorIdempotenciaTest`: mandar o mesmo comando duas vezes grava uma vez só.
