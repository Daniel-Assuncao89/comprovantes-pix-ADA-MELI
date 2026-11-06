# Exercício da Aula 3: o cashback

O negócio pediu: **"todo PIX concluído dá 1% de cashback para o cliente"**.

Vocês vão colocar o cashback no nosso emissor. No caminho, vão ver acontecer, na máquina de vocês, as palavras da aula: **producer, consumer, evento, comando, síncrono, assíncrono, fila, tópico, broker, mensagem, event stream e event sourcing**.

Ninguém precisa saber muito Java: todo código deste exercício está pronto para copiar. O trabalho é **rodar, olhar e explicar**.

## Como trabalhar

- Em **dupla**.
- Cada passo tem quatro partes: **fazer**, **rodar**, **olhar** (o log, a resposta do `curl` ou o banco) e **responder**.
- As respostas vão **neste arquivo**, embaixo de cada pergunta, no lugar do *✍️ escreva aqui*. Com as suas palavras: não precisa ser bonito, precisa estar certo.
- No fim de cada passo, façam um **commit**. A correção abre a próxima aula: tragam a branch de vocês.

## Antes de começar

**1. Criar a branch da dupla e instalar** (uma vez só):

```bash
git checkout aula-03
git checkout -b exercicio-<nome-da-dupla>
mvn install -DskipTests
```

**2. Abrir três terminais Git Bash** (no PowerShell, o `curl` é outro comando):

| Terminal | Para quê | Comando |
|---|---|---|
| 1 | o gravador | `mvn -pl comprovante-gravador spring-boot:run` |
| 2 | o emissor (é aqui que vocês vão olhar o log) | `mvn -pl comprovante-emissor spring-boot:run` |
| 3 | mandar os PIX | os comandos abaixo |

Suba o gravador primeiro e espere aparecer `Started GravadorApplication`. Depois, o emissor.

**3. No terminal 3, guardar o PIX numa variável** (cole tudo de uma vez):

```bash
PIX='{"nome":"Giovanni","tipo_documento":"CPF","numero_documento":"50329291076",
      "numero_agencia":"2022","numero_conta":"00276","digito_verificador_conta":"0",
      "valor_transacao":23.99,"tipo_chave_pix_destino":"CELULAR","chave_pix_destino":"11948755536",
      "nome_cliente_destino":"Fernando","identificacao_pix":"churrasco",
      "data_hora_transacao":"2022-04-10T20:03:57"}'
```

Para mandar um PIX (vocês vão usar muito):

```bash
curl -i -X POST localhost:8081/comprovantes -H 'Content-Type: application/json' -d "$PIX"
```

A primeira linha da resposta é o **status**: `HTTP/1.1 202`, `HTTP/1.1 500`... O `identificador_comprovante` é o id do comprovante.

**4. Reiniciar o emissor depois de mudar o código:** `Ctrl+C` no terminal 2 e o mesmo comando de novo. Ele compila o código novo ao subir.

**5. Olhar os bancos.** Cada serviço tem o seu banco, e cada banco tem um console no navegador:

| | Emissor | Gravador |
|---|---|---|
| Endereço | `http://localhost:8081/h2-console` | `http://localhost:8082/h2-console` |
| JDBC URL | `jdbc:h2:mem:emissor` | `jdbc:h2:mem:gravador` |

Usuário `sa`, senha em branco. **Confira a JDBC URL antes de clicar em Connect:** o console às vezes lembra a do outro serviço. Os bancos ficam na memória: **reiniciar um serviço apaga o banco dele**.

---

## Passo 1: o fluxo de hoje (sem código, ~10 min)

**Faça:** suba o gravador e o emissor e mande um PIX. Olhe o log do emissor (terminal 2). Mande mais um PIX e olhe de novo.

**Dica:** procure as linhas com o id do comprovante. O que aparece entre colchetes, como `[nio-8081-exec-1]` ou `[task-1]`, é o nome da **thread**: quem está executando aquela linha. A thread `nio-8081-exec-N` é a que atende o `curl`.

**1.1** O evento `ComprovanteGravado` tem um **producer** e alguns **consumers**. Quem é o producer? Quem são os consumers?

> ✍️ *escreva aqui*

**1.2** A linha da SAGA ("SAGA concluída") roda na thread `nio-8081-exec-N`. Em qual thread rodam as reações? O que isso quer dizer para o cliente que mandou o PIX?

> ✍️ *escreva aqui*

**1.3** Compare os dois PIX: as reações aparecem sempre na mesma ordem? Por quê?

> ✍️ *escreva aqui*

**1.4** O emissor também chama o gravador. Essa chamada é **síncrona** ou **assíncrona**? Como vocês sabem?

> ✍️ *escreva aqui*

**1.5** O `ComprovanteGravado` é um **comando** ou um **evento**? Por quê?

> ✍️ *escreva aqui*

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

> ✍️ *escreva aqui*

**2.2** Quais arquivos o `git status` mostra? Vocês precisaram mexer no `EmissaoService`? Por quê?

> ✍️ *escreva aqui*

**2.3** O mesmo evento chegou para as quatro reações. Isso se parece mais com uma **fila** ou com um **tópico**? Por quê?

> ✍️ *escreva aqui*

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

> ✍️ *escreva aqui*

**3.2** Sem `@Async`, o PIX foi feito? Olhe o banco. O que o cliente vai pensar? E o que ele provavelmente vai fazer?

> ✍️ *escreva aqui*

**3.3** Sem `@Async`, quais reações apareceram no log para esse PIX? O que aconteceu com as outras?

> ✍️ *escreva aqui*

**3.4** Então: evento é sempre assíncrono?

> ✍️ *escreva aqui*

Faça um commit (com o `Cashback` já desfeito).

---

## Passo 4: o gravador fora do ar (sem código, ~8 min)

**Faça:** pare o gravador (`Ctrl+C` no terminal 1) e mande um PIX. Olhe o status, o log do emissor e o banco do emissor:

```sql
SELECT ID, STATUS, MOTIVO_INVALIDACAO FROM COMPROVANTE_ENTITY;
```

No fim, suba o gravador de novo.

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
- **(b)** Mande outro PIX e, **antes de passar 10 segundos**, pare o emissor (`Ctrl+C` no terminal 2). Anote o id desse comprovante: ele está na resposta do `curl`.
- **(c)** Suba o emissor de novo e espere um pouco. No console do H2 do **gravador**, rode `SELECT * FROM COMPROVANTE_ENTITY;`.
- **(d)** Desfaça: volte o método como era no passo 2 (sem o `Thread.sleep` e sem o `throws InterruptedException`).

**5.1** Em (a), o `curl` demorou 10 segundos para responder? Por quê?

> ✍️ *escreva aqui*

**5.2** Em (b), o PIX foi feito? Como vocês sabem? (Por que olhar o banco do gravador, e não o do emissor?)

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

**Faça:** com o gravador e o emissor de pé, mande um PIX (o **PIX 1**). Pare o gravador, mande outro (o **PIX 2**) e suba o gravador de novo. No console do H2 do **emissor**, rode:

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

Se o **cliente** reenviar o mesmo pedido (o app travou e ele apertou de novo), hoje vira outro PIX, e outro cashback. Faça o emissor aceitar um header `Idempotency-Key` e devolver a **mesma resposta** quando a chave se repetir, sem fazer um PIX novo e sem publicar um segundo evento. Prove com um teste seu.

---

## Se der errado

| O que aparece | O que fazer |
|---|---|
| `Port 8081 was already in use` (ou 8082) | Sobrou um Java rodando: às vezes o `Ctrl+C` no Git Bash não fecha tudo. Rode `netstat -ano \| grep :8081`. O último número da linha é o PID. Depois, `taskkill //PID <o número> //F`. |
| No console do H2: `Table "COMPROVANTE_ENTITY" not found` | A JDBC URL está errada. Emissor: `jdbc:h2:mem:emissor`. Gravador: `jdbc:h2:mem:gravador`. |
| `unreachable statement` ao subir o emissor (passo 3) | Faltou o `if (true)` antes do `throw`. |
| `unreported exception java.lang.InterruptedException` (passo 5) | Faltou o `throws InterruptedException` na linha do método. |
| Mudei o código e nada mudou | Reiniciaram o emissor? |
| `curl` com erro estranho | Use o Git Bash, e não o PowerShell. Confira se a variável `PIX` existe nesse terminal (`echo "$PIX"`). |
| No passo 5, apareceu um `InterruptedException` no log quando o emissor parou | Normal: o emissor interrompeu o cashback que estava esperando. O resultado é o mesmo: o cashback não aconteceu. |
