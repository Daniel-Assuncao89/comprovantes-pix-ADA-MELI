# Comprovantes PIX — AS-IS (exemplo ruim, de propósito)

Esta branch é o **"antes"**: um **monólito** em que tudo está misturado. Um projeto só, um banco só e **uma classe `Comprovante` para todas as áreas**.

Serve para mostrar **por que** o DDD ajuda. O **"depois"** (o to-be) está na branch `gabarito`.

| Branch | O que é |
|---|---|
| `as-is` | **o antes** — monólito com tudo misturado (esta branch) |
| `v0` | o começo do to-be — só a estrutura, com os `TODO` |
| `gabarito` | **o depois** — contextos separados, entidade e Value Objects |

```bash
git checkout as-is      # o antes
git checkout gabarito   # o depois
```

## Os arquivos (são só 4)

```
src/main/java/tech/ada/pix/monolito/
├── Comprovante.java            ← a "entidade Deus" (comece por aqui)
├── ComprovanteService.java     ← o "serviço Deus"
├── ComprovanteController.java
└── ComprovanteRepository.java
```

## O que está errado e como o to-be resolve

| # | Problema no as-is | Onde ver | Como o to-be (`gabarito`) resolve |
|---|---|---|---|
| 1 | **Entidade Deus:** uma classe com dados de emissão, consulta, notificação e antifraude | `Comprovante.java` | Cada contexto com o seu modelo. Notificação e antifraude viram **outros contextos**, que só ficam sabendo pelo evento `ComprovanteGravado` |
| 2 | **Modelo anêmico:** só getters e setters, nenhuma regra na classe | `Comprovante.java` | `ContaBancaria`, `Dinheiro` e `ChavePix` **guardam a própria regra** |
| 3 | **Obsessão por primitivos:** tudo `String` e `double` soltos; `double` não é exato para dinheiro (`0.1 + 0.2` dá `0.30000000000000004`) | `Comprovante.java` | Value Objects; `Dinheiro` usa `BigDecimal` |
| 4 | **Regra espalhada e errada:** agência validada no controller, conta no service, e a conta aceita **6** caracteres (o certo é 5) | controller + service | A regra fica num lugar só (`ContaBancaria`): agência 4, conta 5, dígito 1 |
| 5 | **Cada erro tratado de um jeito:** agência errada dá `400`, conta errada dá `500` | controller + service | Todo campo errado vira `400` (`ComprovanteInvalidoException`) |
| 6 | **Tudo na mesma transação:** se o SMS cair, **o comprovante nem é salvo** | `ComprovanteService.emitir()` | A emissão só aceita (`202`); o resto reage depois, sem derrubar a emissão (Aulas 4 a 6) |
| 7 | **Um banco para tudo:** a mesma tabela serve emissão, consulta, notificação e antifraude | `application.yml` | Cada serviço com o seu banco (ADR-001) |
| 8 | **A consulta escreve no banco:** cada `GET` grava um contador | `ComprovanteService.consultar()` | A consulta só lê (e ganha cache na Aula 3) |
| 9 | **Vaza dado interno:** o `POST` responde `201` devolvendo a entidade inteira, com o score do antifraude | `ComprovanteController.emitir()` | Responde `202` só com o id e a data |

## Os testes provam cada dor

```bash
mvn test
```

| Teste | O que mostra |
|---|---|
| `AcoplamentoTest` | Com o SMS fora do ar, o `POST` dá `500` e **nenhum comprovante é salvo** (problema 6) |
| `EntidadeDeusTest` | A resposta vaza `score_risco` (9), conta com 6 caracteres passa (4) e cada erro sai com um código diferente (5) |

## Como rodar

```bash
mvn spring-boot:run      # sobe na porta 8080
```

Use o mesmo `curl` do `gabarito`, trocando a porta para `8080`. Para ver a dor do acoplamento ao vivo, mude `notificacao.gateway-fora` para `true` no `application.yml`, rode de novo e mande o `POST`.

## Sugestão de uso em aula

1. Mostre o `Comprovante.java` e pergunte: **"o que tem de errado aqui?"**
2. Repare com a turma nos **blocos de campos** (emissão, consulta, notificação, antifraude): cada bloco é um **bounded context escondido**. É ótimo para fazer o event storming em cima.
3. Rode o `AcoplamentoTest`: *"o SMS caiu e o cliente ficou sem comprovante"*.
4. Troque para a branch `gabarito` e mostre cada problema resolvido, seguindo a tabela acima.
