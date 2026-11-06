# Comprovantes PIX — Aulas 1 (DDD), 2 (SAGA) e 3 (comunicação)

Exemplo do projeto guiado. Uma API que **emite** e **consulta** comprovantes de PIX que já aconteceram.

**O exercício da Aula 3 está em [`EXERCICIO-AULA-03.md`](EXERCICIO-AULA-03.md).**

## Comece aqui

Você só precisa de duas coisas instaladas:

- o **[Git](https://git-scm.com/downloads)**. No Windows, ele traz o **Git Bash**: use ele como terminal;
- o **[Docker Desktop](https://www.docker.com/products/docker-desktop/)**, **aberto**.

Não precisa instalar Java nem Maven: eles rodam dentro do Docker.

**1. Baixe o projeto.** O endereço está no botão verde **Code**, aqui no GitHub:

```bash
git clone <o endereço do botão Code>
cd <a pasta que o git criou>
```

**2. Suba o gravador** (em segundo plano):

```bash
docker compose up -d --build gravador
```

**3. Suba o emissor.** Este terminal fica mostrando o log dele:

```bash
docker compose up --build emissor
```

Está pronto quando aparecer `Started EmissorApplication`. **A primeira vez demora uns minutos**: o Docker baixa o Java, o Maven e as bibliotecas. Se puder, faça isso **antes da aula**, numa internet boa. Depois, fica rápido.

**4. Em outro terminal**, na mesma pasta, mande um PIX. O PIX está no arquivo [`pix.json`](pix.json):

```bash
curl -i -X POST localhost:8081/comprovantes -H "Content-Type: application/json" -d "@pix.json"
```

A primeira linha da resposta deve ser `HTTP/1.1 202`. No log do emissor, aparecem a SAGA e as reações (SMS, antifraude e BI).

> **No PowerShell**, troque `curl` por `curl.exe`. O resto do comando é igual.

### Os comandos do dia a dia

| Para... | Comando |
|---|---|
| reiniciar o emissor depois de mudar o código | `Ctrl+C` no terminal do emissor, e `docker compose up --build emissor` de novo |
| desligar o gravador | `docker compose stop gravador` |
| ligar o gravador de novo | `docker compose start gravador` |
| derrubar o emissor de repente, como uma queda | `docker compose kill emissor` |
| ver o log do gravador | `docker compose logs gravador` |
| ver o que está rodando | `docker compose ps` |
| rodar os testes | `docker compose run --rm testes` |
| desligar tudo | `docker compose down` |

### Os bancos

Cada serviço tem o seu banco (H2), e cada banco tem um console no navegador:

| | Emissor | Gravador |
|---|---|---|
| Console | `http://localhost:8081/h2-console` | `http://localhost:8082/h2-console` |
| JDBC URL | `jdbc:h2:mem:emissor` | `jdbc:h2:mem:gravador` |

Usuário `sa`, senha em branco. **Confira a JDBC URL antes de clicar em Connect**: o console às vezes lembra a do outro serviço. Os bancos ficam na memória: **reiniciar um serviço apaga o banco dele**.

### Sem Docker (com Java 21 e Maven)

Quem já tem **Java 21** e **Maven** pode rodar direto na máquina. Instale uma vez (e de novo sempre que trocar de branch). Sem isso, o emissor não acha o `shared-contracts`:

```bash
mvn install -DskipTests
```

Depois, troque os comandos do Docker por estes:

| Com Docker | Sem Docker |
|---|---|
| `docker compose up -d --build gravador` | `mvn -pl comprovante-gravador spring-boot:run`, num terminal só para ele |
| `docker compose up --build emissor` | `mvn -pl comprovante-emissor spring-boot:run` |
| `docker compose stop gravador` | `Ctrl+C` no terminal do gravador |
| `docker compose start gravador` | o comando do gravador de novo |
| `docker compose kill emissor` | `Ctrl+C` no terminal do emissor |
| `docker compose run --rm testes` | `mvn test` |

No Git Bash, às vezes o `Ctrl+C` não fecha o Java, e ele não sobe de novo (`Port 8081 was already in use`). Rode `netstat -ano | grep :8081`: o último número da linha é o PID. Depois, `taskkill //PID <o número> //F`.

## As branches

| Branch | O que tem |
|---|---|
| `main` | **O ponto de partida da aula de agora.** Hoje, é igual à `aula-03`. |
| `as-is` | O "antes" da Aula 1: um monólito ruim de propósito, para a turma criticar. |
| `v0` | Só a **estrutura**: os módulos, as classes vazias e os `// TODO`. É o ponto de partida da Aula 1. |
| `gabarito` | Fim da Aula 1: validação no emissor + o modelo de domínio com **entidade** e **Value Objects**. |
| `aula-02` | Fim da Aula 2: o emissor chama o gravador numa **SAGA** e sabe desfazer quando a gravação falha. |
| `aula-03` | Fim da Aula 3: no fim da SAGA, o emissor **avisa** com um evento, e SMS, antifraude e BI reagem cada um por conta própria. |

A correção de cada exercício entra numa branch com `-gabarito` no nome, publicada **depois** da correção em aula.

O Docker existe a partir da `aula-03`. Nas branches anteriores, rode sem Docker.

Para trocar de uma branch para a outra:

```bash
git switch aula-02
git switch aula-03
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
| `comprovante-consulta` | 8083 | Responde o `GET /comprovantes/{id}` (por enquanto sempre `404`; ainda não está no Docker) |
| `shared-contracts` | — | Classes que os serviços trocam entre si (os "envelopes" de dados) |

```
.
├── pom.xml                    # projeto "pai", junta os módulos
├── Dockerfile                 # como montar a imagem de cada serviço
├── docker-compose.yml         # sobe o gravador e o emissor (e roda os testes)
├── pix.json                   # o PIX de exemplo, para o curl
├── EXERCICIO-AULA-03.md       # o exercício da Aula 3
├── shared-contracts/
├── comprovante-emissor/
├── comprovante-gravador/
├── comprovante-consulta/
└── docs/
    ├── arquitetura.md         # desenho dos serviços e de como eles crescem nas aulas
    └── adr/                   # decisões registradas (ADR-001: cada serviço com seu banco)
```

**O que o emissor responde:**

| Situação | Resposta do emissor |
|---|---|
| Tudo certo | `202`, e o comprovante fica `GRAVADO` |
| Campo errado (troque `"numero_conta": "00276"` por `"002761"` no `pix.json`) | `400`, com o campo errado na `message`, e ninguém chama o gravador |
| Gravador desligado (`docker compose stop gravador`) | `503`, com o id do comprovante na `message`, e o comprovante fica `INVALIDADO` (a SAGA desfez) |

## As demos da Aula 2

**Gravador lento** ("demorar não quer dizer não"). Suba o gravador com um atraso maior que o tempo de espera do emissor (2 segundos):

```bash
DEMO_ATRASO_MS=3000 docker compose up -d gravador
```

O emissor desiste, responde `503` e marca o comprovante como `INVALIDADO`... mas o gravador **salvou**. Para ver, abra os **dois consoles do H2** e rode `SELECT * FROM COMPROVANTE_ENTITY;` nos dois: o emissor mostra o comprovante `INVALIDADO`, com o motivo, e o gravador mostra o mesmo id, **gravado**.

Para voltar ao normal: `docker compose up -d gravador`.

**Gravador idempotente.** Mande o mesmo comprovante direto para o gravador duas vezes (no Git Bash). A primeira responde `201` (gravei agora), a segunda `200` (já tinha):

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
DEMO_SMS_FORA=true docker compose up --build emissor
```

- **Como está na branch (com `@Async`):** o `POST` responde `202`. O erro do SMS aparece só no log, numa thread `task-N`. O PIX não cai junto.
- **Tire o `@Async` da `NotificacaoSms` e suba de novo:** o `POST` responde `500` ("operadora de SMS fora do ar")... mas o comprovante ficou `GRAVADO` nos dois bancos. Evento **não** quer dizer assíncrono: são duas decisões diferentes.

No PowerShell, a variável é assim: `$env:DEMO_SMS_FORA="true"; docker compose up --build emissor`. Para voltar ao normal, feche o terminal (ou `$env:DEMO_SMS_FORA="false"`) e suba de novo.

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

**Docker**: o `Dockerfile` e o `docker-compose.yml` sobem o gravador e o emissor sem instalar Java nem Maven. Dentro do Docker, o emissor acha o gravador pelo nome do serviço (`http://gravador:8082`), e não por `localhost`.

**Testes** (`docker compose run --rm testes`, ou `mvn test`; 17 no total):
- `EventoComprovanteGravadoTest`: a SAGA terminou → **um** evento, com o valor. A SAGA compensou → **nenhum** evento.
- `SmsForaTest`: com o SMS fora, o PIX continua `GRAVADO`. Quebra se alguém tirar o `@Async`. (Por isso aparece um `ERROR` do SMS no meio dos testes: é de propósito.)

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
