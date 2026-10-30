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
 */
public class Comprovante {

    private final UUID id;
    private final String nome;
    private final ContaBancaria contaOrigem;
    private final Dinheiro valor;
    private final ChavePix chaveDestino;

    private Comprovante(UUID id, String nome, ContaBancaria contaOrigem, Dinheiro valor, ChavePix chaveDestino) {
        this.id = id;
        this.nome = nome;
        this.contaOrigem = contaOrigem;
        this.valor = valor;
        this.chaveDestino = chaveDestino;
    }

    public static Comprovante emitir(String nome, ContaBancaria contaOrigem, Dinheiro valor, ChavePix chaveDestino) {
        if (nome == null || nome.isBlank())
            throw new ComprovanteInvalidoException("nome é obrigatório");
        if (contaOrigem == null || valor == null || chaveDestino == null)
            throw new ComprovanteInvalidoException("comprovante incompleto");
        return new Comprovante(UUID.randomUUID(), nome, contaOrigem, valor, chaveDestino);
    }

    public UUID id() { return id; }
    public String nome() { return nome; }
    public ContaBancaria contaOrigem() { return contaOrigem; }
    public Dinheiro valor() { return valor; }
    public ChavePix chaveDestino() { return chaveDestino; }

    @Override
    public boolean equals(Object o) {
        return o instanceof Comprovante outro && id.equals(outro.id);
    }

    @Override
    public int hashCode() {
        return id.hashCode();
    }
}
