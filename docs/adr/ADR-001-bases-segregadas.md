# ADR-001 — Bases de dados segregadas por serviço

## Contexto
A solução de comprovantes tem três responsabilidades (emissão, gravação, consulta). Precisamos decidir o modelo de dados entre os serviços. (Aula 1 — DDD.)

## Decisão
Cada microsserviço dono do seu dado tem **sua própria base** (H2 segregado no esqueleto). Nenhum serviço lê a tabela do outro diretamente; integração é por **contrato** (mensagem/evento/REST).

## Alternativas consideradas
- **Base compartilhada** — simples no começo, mas cria acoplamento por dados: mudança de schema quebra todos, e os serviços deixam de ser independentes. **Rejeitada.**
- **Base por serviço** — mais peças e consistência eventual entre elas, mas independência real de evolução e deploy. **Escolhida.**

## Consequências
- Ganhamos isolamento e autonomia de cada bounded context.
- Pagamos com **consistência eventual** (tratada via SAGA na Aula 2) e necessidade de propagar dados por eventos (Aula 6).

## Status
Aceita
