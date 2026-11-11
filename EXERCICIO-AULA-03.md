# Exercício da Aula 3: o cashback

O negócio pediu: **"todo PIX concluído dá 1% de cashback para o cliente"**.

Você vai colocar o cashback no nosso emissor. No caminho, vai ver acontecer, na sua máquina, as palavras da aula: **producer, consumer, evento, comando, síncrono, assíncrono, fila, tópico, broker, mensagem, event stream e event sourcing**.

Ninguém precisa saber muito Java: todo código deste exercício está pronto para copiar. O trabalho é **rodar, olhar e explicar**.

## Como trabalhar

- **Individual**: cada pessoa faz o seu, na sua máquina.
- Cada passo tem quatro partes: **fazer**, **rodar**, **olhar** (o log, a resposta do `curl` ou o banco) e **responder**.
- As respostas vão **neste arquivo**, embaixo de cada pergunta, no lugar do *✍️ escreva aqui*. Com as suas palavras: não precisa ser bonito, precisa estar certo.
- No fim de cada passo, faça um **commit**. A correção abre a próxima aula: traga a sua branch.

## Antes de começar

**0. O que precisa estar instalado:** o Git e o Docker Desktop, **aberto**. O [README](README.md), na seção "Comece aqui", explica. Quem já tem Java 21 e Maven pode rodar sem Docker: o README tem a troca dos comandos.

**1. Criar a sua branch** (uma vez só):

```bash
git switch aula-03
git switch -c exercicio-<seu-nome>
```

**2. Abrir dois terminais**, os dois na pasta do projeto. No Windows, use o **Git Bash**.

| Terminal | Para quê |
|---|---|
| 1 | o **emissor**. É aqui que você vai olhar o log |
| 2 | **todo o resto**: o gravador, os PIX e o `git` |

Suba o gravador primeiro, no terminal 2:

```bash
docker compose up -d --build gravador
```

Depois, o emissor, no terminal 1. Espere aparecer `Started EmissorApplication`:

```bash
docker compose up --build emissor
```

A primeira vez demora uns minutos: o Docker baixa o Java, o Maven e as bibliotecas. Depois, fica rápido.

**3. Mandar um PIX** (você vai usar muito), no terminal 2:

```bash
curl -i -X POST localhost:8081/comprovantes -H "Content-Type: application/json" -d "@pix.json"
```

O PIX está no arquivo `pix.json`, na pasta do projeto. A primeira linha da resposta é o **status**: `HTTP/1.1 202`, `HTTP/1.1 500`... O `identificador_comprovante` é o id do comprovante. (No PowerShell, troque `curl` por `curl.exe`.)

**4. Reiniciar o emissor depois de mudar o código:** `Ctrl+C` no terminal 1 e o mesmo comando de novo, `docker compose up --build emissor`. O `--build` compila o código novo: leva uns 20 segundos.

**5. Ligar e desligar os serviços**, no terminal 2:

| Para... | Comando |
|---|---|
| desligar o gravador | `docker compose stop gravador` |
| ligar o gravador de novo | `docker compose start gravador` |
| derrubar o emissor de repente, como uma queda | `docker compose kill emissor` |

**6. Olhar os bancos.** Cada serviço tem o seu banco, e cada banco tem um console no navegador:

| | Emissor | Gravador |
|---|---|---|
| Endereço | `http://localhost:8081/h2-console` | `http://localhost:8082/h2-console` |
| JDBC URL | `jdbc:h2:mem:emissor` | `jdbc:h2:mem:gravador` |

Usuário `sa`, senha em branco. **Confira a JDBC URL antes de clicar em Connect:** o console às vezes lembra a do outro serviço. Os bancos ficam na memória: **reiniciar um serviço apaga o banco dele**.

No fim da aula, `docker compose down` desliga tudo.

---

## Passo 1: o fluxo de hoje (sem código, ~10 min)

**Faça:** suba o gravador e o emissor e mande um PIX. Olhe o log do emissor (terminal 1). Mande mais um PIX e olhe de novo.

**Dica:** procure as linhas com o id do comprovante. O que aparece entre colchetes, como `[nio-8081-exec-1]` ou `[task-1]`, é o nome da **thread**: quem está executando aquela linha. A thread `nio-8081-exec-N` é a que atende o `curl`. (O `emissor-1 |` no começo de cada linha é o Docker dizendo de qual container ela veio.)

**1.1** O evento `ComprovanteGravado` tem um **producer** e alguns **consumers**. Quem é o producer? Quem são os consumers?

> O producer é o `EmissaoService`: depois de gravar o comprovante, ele publica `ComprovanteGravado`. Os consumers são as reações que escutam esse evento: `NotificacaoSms`, `Antifraude` e `PainelBi`.

**1.2** A linha da SAGA ("SAGA concluída") roda na thread `nio-8081-exec-N`. Em qual thread rodam as reações? O que isso quer dizer para o cliente que mandou o PIX?

> As reações rodam em threads `task-N`, diferentes da `nio-8081-exec-N` que atende o `curl`. Como são assíncronas, o cliente espera a conclusão da SAGA, mas não precisa esperar as reações; uma falha nelas não altera a resposta do PIX.

**1.3** Compare os dois PIX: as reações aparecem sempre na mesma ordem? Por quê?

> Não. Como cada reação é executada de forma assíncrona em uma thread, elas podem concorrer e terminar em ordens diferentes. Por isso, a ordem das linhas no log pode variar entre os PIX.

**1.4** O emissor também chama o gravador. Essa chamada é **síncrona** ou **assíncrona**? Como você sabe?

> É síncrona. O emissor faz uma chamada HTTP ao gravador e espera a resposta antes de concluir a SAGA, publicar `ComprovanteGravado` e responder ao cliente.

**1.5** O `ComprovanteGravado` é um **comando** ou um **evento**? Por quê?

> É um evento: comunica um fato que já aconteceu — o comprovante foi gravado. Não é uma instrução para alguém gravá-lo; é publicado depois que a gravação foi concluída.

---

## Passo 2: o cashback (~12 min)

**Faça:** crie o arquivo `comprovante-emissor/src/main/java/tech/ada/pix/emissor/reacoes/Cashback.java` com o conteúdo abaixo. É uma cópia do `PainelBi.java`, com outro nome e outra mensagem.

```java
package tech.ada.pix.emissor.reacoes;

import tech.ada.pix.emissor.dominio.ComprovanteGravado;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

/**
 * Reação ao ComprovanteGravado (exercício da Aula 3): o negócio pediu cashback em todo PIX.
 * Aqui é só um log.
 *
 * Repare: para o cashback existir, ninguém mexeu no EmissaoService. Ele continua publicando
 * o mesmo evento, sem saber quem escuta. Esta classe é só mais um interessado.
 */
@Component
public class Cashback {

    private static final Logger log = LoggerFactory.getLogger(Cashback.class);

    @Async
    @EventListener
    public void quandoGravado(ComprovanteGravado evento) {
        log.info("Cashback: 1% de R$ {} vai voltar para o cliente (comprovante {})",
                evento.valor().valor(), evento.idComprovante());
    }
}
```

Reinicie o emissor, mande um PIX e olhe o log. Depois, rode `git status`.

**2.1** Quantas reações apareceram no log agora?

> Quatro: SMS, antifraude, BI e cashback.

**2.2** Quais arquivos o `git status` mostra? Você precisou mexer no `EmissaoService`? Por quê?

> O `git status` mostra o novo `Cashback.java` e o `EXERCICIO-AULA-03.md` modificado com estas respostas. Não precisei mexer no `EmissaoService`: ele já publica o evento `ComprovanteGravado`, e o cashback é outra reação inscrita para recebê-lo.

**2.3** O mesmo evento chegou para as quatro reações. Isso se parece mais com uma **fila** ou com um **tópico**? Por quê?

> Parece mais com um tópico: cada reação inscrita recebe uma cópia do mesmo evento. Em uma fila de consumidores concorrentes, em geral uma mensagem é entregue a apenas um consumidor.

Faça um commit.

---

## Passo 3: o cashback fora do ar (~12 min)

Este passo tem três partes.

**(a)** No `Cashback`, coloque esta linha como a **primeira** dentro do método `quandoGravado`:

```java
        if (true) throw new IllegalStateException("serviço de cashback fora do ar");
```

O `if (true)` está ali só para o Java aceitar compilar a linha de baixo. Reinicie o emissor e mande um PIX. Anote o status e olhe o log.

**(b)** Agora **apague a linha do `@Async`** do `Cashback`. Reinicie o emissor e mande outro PIX. Anote o status e olhe o log. Depois, no console do H2 do **emissor**, rode:

```sql
SELECT ID, STATUS FROM COMPROVANTE_ENTITY;
```

**(c)** Desfaça: coloque o `@Async` de volta e apague a linha do `if (true) throw ...`.

**3.1** Com `@Async`, qual status o `curl` recebeu? E sem `@Async`?

> Com `@Async`, recebeu `202`: a exceção aconteceu na thread da reação e não mudou a resposta do emissor. Sem `@Async`, recebeu `500`, porque a exceção foi lançada durante o processamento da requisição.

**3.2** Sem `@Async`, o PIX foi feito? Olhe o banco. O que o cliente vai pensar? E o que ele provavelmente vai fazer?

> Sim. No banco do emissor o comprovante está `GRAVADO` (e o gravador também recebeu o PIX), pois a falha do cashback acontece depois de a SAGA concluir. Como recebeu `500`, o cliente pode pensar que o PIX falhou e tentar fazê-lo de novo, arriscando uma operação duplicada.

**3.3** Sem `@Async`, quais reações apareceram no log para esse PIX? O que aconteceu com as outras?

> Apareceram SMS, antifraude e BI. O cashback lançou a exceção antes de registrar seu log de sucesso. Neste teste, as outras três reações já tinham sido despachadas e executaram; a exceção síncrona não as desfez.

**3.4** Então: evento é sempre assíncrono?

> Não. Publicar um evento não o torna assíncrono por si só. Neste exemplo, o Spring chama os listeners sincronamente por padrão; `@Async` é o que faz cada reação anotada rodar em outra thread.

Faça um commit (com o `Cashback` já desfeito).

---

## Passo 4: o gravador fora do ar (sem código, ~8 min)

**Faça:** pare o gravador (`docker compose stop gravador`) e mande um PIX. O `curl` demora uns segundos para responder. Olhe o status, o log do emissor e o banco do emissor:

```sql
SELECT ID, STATUS, MOTIVO_INVALIDACAO FROM COMPROVANTE_ENTITY;
```

No fim, suba o gravador de novo (`docker compose start gravador`). Ele leva uns 15 segundos para ficar pronto.

**4.1** Qual status o `curl` recebeu? Como ficou o comprovante no banco?

> ✍️ *escreva aqui*

**4.2** O cashback rodou para esse PIX? Por quê?

> ✍️ *escreva aqui*

**4.3** No passo 3, o cashback caiu e o PIX passou (com `@Async`). Aqui, o gravador caiu e o PIX não passou. Por que a diferença?

> ✍️ *escreva aqui*

**4.4** Desenhe aqui, com texto mesmo, como ficaria com uma **fila** entre o emissor e o gravador. Com o gravador fora, o que o `curl` receberia? E o que aconteceria quando o gravador voltasse?

> ✍️ *escreva aqui*

---

## Passo 5: o evento que sumiu (~12 min)

**Faça:** no `Cashback`, troque o método `quandoGravado` inteiro por este. Agora o cashback "demora" 10 segundos:

```java
    @Async
    @EventListener
    public void quandoGravado(ComprovanteGravado evento) throws InterruptedException {
        Thread.sleep(10_000);   // ⚠️ só para o passo 5: finge que o cashback demora 10 segundos
        log.info("Cashback: 1% de R$ {} vai voltar para o cliente (comprovante {})",
                evento.valor().valor(), evento.idComprovante());
    }
```

Reinicie o emissor. Depois:

- **(a)** Mande um PIX e espere 10 segundos. Olhe o log.
- **(b)** Mande outro PIX e, **antes de passar 10 segundos**, derrube o emissor. Para dar tempo, mande os dois comandos juntos, numa linha só:
  ```bash
  curl -i -X POST localhost:8081/comprovantes -H "Content-Type: application/json" -d "@pix.json"; docker compose kill emissor
  ```
  Anote o id desse comprovante: ele está na resposta do `curl`.
- **(c)** Suba o emissor de novo (no terminal 1, `docker compose up --build emissor`) e espere uns 15 segundos. No console do H2 do **gravador**, rode `SELECT * FROM COMPROVANTE_ENTITY;`.
- **(d)** Desfaça: volte o método como era no passo 2 (sem o `Thread.sleep` e sem o `throws InterruptedException`).

**5.1** Em (a), o `curl` demorou 10 segundos para responder? Por quê?

> ✍️ *escreva aqui*

**5.2** Em (b), o PIX foi feito? Como você sabe? (Por que olhar o banco do gravador, e não o do emissor?)

> ✍️ *escreva aqui*

**5.3** O cashback do PIX de (b) aconteceu? Vai acontecer agora que o emissor voltou? Por quê?

> ✍️ *escreva aqui*

**5.4** Onde o evento precisaria estar guardado para não sumir?

> ✍️ *escreva aqui*

**5.5** Imagine que o cashback teve um bug e calculou errado por 3 dias. Com o que temos hoje, dá para recalcular? E se os eventos estivessem num **event stream** (como o Kafka)?

> ✍️ *escreva aqui*

Faça um commit (com o `Cashback` já desfeito).

---

## Passo 6: estado × fatos (SQL e papel, ~10 min)

**Faça:** com o gravador e o emissor de pé, mande um PIX (o **PIX 1**). Pare o gravador (`docker compose stop gravador`), mande outro (o **PIX 2**) e suba o gravador de novo (`docker compose start gravador`). No console do H2 do **emissor**, rode:

```sql
SELECT * FROM COMPROVANTE_ENTITY;
```

**6.1** Quantas linhas tem a tabela? Qual é o `STATUS` de cada uma?

> ✍️ *escreva aqui*

**6.2** Durante a SAGA, os dois comprovantes passaram pelo status `ACEITO`. A tabela mostra isso? Mostra a que horas cada coisa aconteceu?

> ✍️ *escreva aqui*

**6.3** Escreva a lista de **fatos** (eventos) desses dois PIX, em ordem, como se o emissor guardasse uma tabela `eventos`. Uma linha por fato: o comprovante, o nome do evento e o que mais for importante.

> ✍️ *escreva aqui*

**6.4** Se o emissor usasse **event sourcing**, qual das duas seria a verdade: a `COMPROVANTE_ENTITY` ou a lista de fatos? Como ele saberia o status de agora do PIX 2?

> ✍️ *escreva aqui*

Faça um commit.

---

## Desafio extra (para quem já sabe Java)

Se o **cliente** reenviar o mesmo pedido (o app travou e ele apertou de novo), hoje vira outro PIX, e outro cashback. Faça o emissor aceitar um header `Idempotency-Key` e devolver a **mesma resposta** quando a chave se repetir, sem fazer um PIX novo e sem publicar um segundo evento. Prove com um teste seu. Para rodar os testes: `docker compose run --rm testes`.

---

## Se der errado

| O que aparece | O que fazer |
|---|---|
| `Cannot connect to the Docker daemon`, `error during connect` ou `docker: command not found` | O Docker Desktop não está aberto. Abra e espere ele dizer que está rodando (*Engine running*). |
| `port is already allocated` (8081 ou 8082) | Outro programa está usando a porta: talvez um emissor rodando fora do Docker. Feche esse programa. O `docker compose ps` mostra o que está rodando no Docker. |
| `Empty reply from server` ou `Connection refused` | O serviço ainda está subindo. Espere o `Started ...Application` no log e mande de novo. |
| `503` logo depois de ligar o gravador de novo | O gravador leva uns 15 segundos para ficar pronto. Espere e mande de novo. |
| No console do H2: `Table "COMPROVANTE_ENTITY" not found` | A JDBC URL está errada. Emissor: `jdbc:h2:mem:emissor`. Gravador: `jdbc:h2:mem:gravador`. |
| `unreachable statement` ao subir o emissor (passo 3) | Faltou o `if (true)` antes do `throw`. O erro aparece no meio do build, numa linha com `[ERROR]`. |
| `unreported exception java.lang.InterruptedException` (passo 5) | Faltou o `throws InterruptedException` na linha do método. |
| Mudei o código e nada mudou | Reiniciaram o emissor **com o `--build`**? |
| `curl` com erro estranho | No PowerShell, use `curl.exe`. E rode o comando na pasta do projeto: o `@pix.json` é o arquivo de lá. |
| `the input device is not a TTY` | Rode o mesmo comando com `-T` depois do `run`: `docker compose run --rm -T testes`. |
| Sem Docker, no passo 5, apareceu um `InterruptedException` no log quando o emissor parou | Normal: o emissor interrompeu o cashback que estava esperando. O resultado é o mesmo: o cashback não aconteceu. |
