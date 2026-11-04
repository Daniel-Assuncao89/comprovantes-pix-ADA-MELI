# Comprovantes PIX — Aulas 1 (DDD) e 2 (SAGA)

Exemplo do projeto guiado. Uma API que **emite** e **consulta** comprovantes de PIX que já aconteceram.

## As branches

| Branch | O que tem |
|---|---|
| `as-is` | O "antes" da Aula 1: um monólito ruim de propósito, para a turma criticar. |
| `v0` | Só a **estrutura**: os módulos, as classes vazias e os `// TODO`. É o ponto de partida da Aula 1. |
| `gabarito` | Fim da Aula 1: validação no emissor + o modelo de domínio com **entidade** e **Value Objects**. |
| `aula-02` | Fim da Aula 2: o emissor chama o gravador numa **SAGA** e sabe desfazer quando a gravação falha. |

Para trocar de uma para a outra:

```bash
git checkout gabarito
git checkout aula-02
```

Para ver tudo o que muda entre duas branches:

```bash
git diff gabarito aula-02
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

## O que mudou nesta branch (`aula-02`)

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

**Desafio para os alunos:** se o **cliente** reenviar o mesmo pedido (porque a resposta não chegou), hoje vira outro comprovante, com outro id. Faça o emissor aceitar um header `Idempotency-Key` e devolver a mesma resposta quando a chave se repetir.
