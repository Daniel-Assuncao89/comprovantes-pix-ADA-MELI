package tech.ada.pix.emissor.dominio;

import tech.ada.pix.emissor.ComprovanteInvalidoException;

import java.util.UUID;

/**
 * Entidade (e raiz do agregado): o comprovante do PIX.
 *
 * Tem identidade (UUID) — dois comprovantes com os mesmos dados, mas ids
 * diferentes, são comprovantes DIFERENTES. Por isso equals/hashCode usam só o id.
 *
 * Só nasce pelo método emitir(...): ninguém cria um comprovante "pela metade".
 *
 * Aula 2: ganhou um CICLO DE VIDA (status). Os dados continuam imutáveis; o que muda
 * é em que pé ele está na SAGA. Quem decide se a mudança é permitida é a própria entidade.
 */
public class Comprovante {

    private final UUID id;
    private final String nome;
    private final ContaBancaria contaOrigem;
    private final Dinheiro valor;
    private final ChavePix chaveDestino;

    private StatusComprovante status;
    private String motivoInvalidacao;

    private Comprovante(UUID id, String nome, ContaBancaria contaOrigem, Dinheiro valor, ChavePix chaveDestino) {
        this.id = id;
        this.nome = nome;
        this.contaOrigem = contaOrigem;
        this.valor = valor;
        this.chaveDestino = chaveDestino;
        this.status = StatusComprovante.ACEITO;
    }

    public static Comprovante emitir(String nome, ContaBancaria contaOrigem, Dinheiro valor, ChavePix chaveDestino) {
        if (nome == null || nome.isBlank())
            throw new ComprovanteInvalidoException("nome é obrigatório");
        if (contaOrigem == null || valor == null || chaveDestino == null)
            throw new ComprovanteInvalidoException("comprovante incompleto");
        return new Comprovante(UUID.randomUUID(), nome, contaOrigem, valor, chaveDestino);
    }

    /**
     * Monta de volta um comprovante que JÁ EXISTIA (lido da base do emissor).
     * Não é um comprovante novo: por isso não gera id e volta no status em que estava.
     */
    public static Comprovante reconstituir(UUID id, String nome, ContaBancaria contaOrigem, Dinheiro valor,
                                           ChavePix chaveDestino, StatusComprovante status, String motivoInvalidacao) {
        Comprovante comprovante = new Comprovante(id, nome, contaOrigem, valor, chaveDestino);
        comprovante.status = status;
        comprovante.motivoInvalidacao = motivoInvalidacao;
        return comprovante;
    }

    /** O passo "gravar" da SAGA deu certo. */
    public void confirmarGravacao() {
        if (status == StatusComprovante.INVALIDADO)
            throw new IllegalStateException("comprovante " + id + " foi invalidado e não pode ser gravado");
        status = StatusComprovante.GRAVADO;
    }

    /**
     * AÇÃO COMPENSATÓRIA da SAGA.
     *
     * Não é um DELETE: o comprovante continua existindo, marcado como INVALIDADO e com o
     * motivo (em banco, a gente não apaga: deixa rastro).
     * É idempotente: invalidar duas vezes dá no mesmo que invalidar uma.
     */
    public void invalidar(String motivo) {
        if (status == StatusComprovante.INVALIDADO) return; // já compensado: nada a fazer
        if (status == StatusComprovante.GRAVADO)
            throw new IllegalStateException("comprovante " + id + " já foi gravado e não pode ser invalidado");
        status = StatusComprovante.INVALIDADO;
        motivoInvalidacao = motivo;
    }

    public UUID id() { return id; }
    public String nome() { return nome; }
    public ContaBancaria contaOrigem() { return contaOrigem; }
    public Dinheiro valor() { return valor; }
    public ChavePix chaveDestino() { return chaveDestino; }
    public StatusComprovante status() { return status; }
    public String motivoInvalidacao() { return motivoInvalidacao; }

    @Override
    public boolean equals(Object o) {
        return o instanceof Comprovante outro && id.equals(outro.id);
    }

    @Override
    public int hashCode() {
        return id.hashCode();
    }
}
