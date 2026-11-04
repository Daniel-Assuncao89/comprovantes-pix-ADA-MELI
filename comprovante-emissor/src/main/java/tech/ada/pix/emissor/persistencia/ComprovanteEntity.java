package tech.ada.pix.emissor.persistencia;

import tech.ada.pix.contracts.TipoChavePix;
import tech.ada.pix.emissor.dominio.ChavePix;
import tech.ada.pix.emissor.dominio.Comprovante;
import tech.ada.pix.emissor.dominio.ContaBancaria;
import tech.ada.pix.emissor.dominio.Dinheiro;
import tech.ada.pix.emissor.dominio.StatusComprovante;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;

import java.math.BigDecimal;
import java.util.UUID;

/**
 * O "formato de guardar" o comprovante na base do emissor (uma linha da tabela).
 *
 * Fica SEPARADO do {@link Comprovante} de propósito: o modelo de domínio da Aula 1
 * (entidade + Value Objects) continua sem nenhuma anotação de banco.
 * Aqui os VOs viram colunas simples; na volta, viram VOs de novo.
 */
@Entity
public class ComprovanteEntity {

    @Id
    private UUID id;

    private String nome;
    private String numeroAgencia;
    private String numeroConta;
    private String digitoVerificadorConta;

    @Column(precision = 15, scale = 2)
    private BigDecimal valorTransacao;

    @Enumerated(EnumType.STRING)
    private TipoChavePix tipoChavePixDestino;
    private String chavePixDestino;

    @Enumerated(EnumType.STRING)
    private StatusComprovante status;          // em que pé o comprovante está na SAGA

    @Column(length = 1000)
    private String motivoInvalidacao;          // o rastro da compensação

    protected ComprovanteEntity() {
        // exigido pelo JPA
    }

    /** Domínio → linha da tabela. */
    static ComprovanteEntity de(Comprovante c) {
        ComprovanteEntity e = new ComprovanteEntity();
        e.id = c.id();
        e.nome = c.nome();
        e.numeroAgencia = c.contaOrigem().agencia();
        e.numeroConta = c.contaOrigem().conta();
        e.digitoVerificadorConta = c.contaOrigem().digito();
        e.valorTransacao = c.valor().valor();
        e.tipoChavePixDestino = c.chaveDestino().tipo();
        e.chavePixDestino = c.chaveDestino().valor();
        e.status = c.status();
        e.motivoInvalidacao = c.motivoInvalidacao();
        return e;
    }

    /** Linha da tabela → domínio. */
    Comprovante paraDominio() {
        return Comprovante.reconstituir(
                id,
                nome,
                new ContaBancaria(numeroAgencia, numeroConta, digitoVerificadorConta),
                new Dinheiro(valorTransacao),
                new ChavePix(tipoChavePixDestino, chavePixDestino),
                status,
                motivoInvalidacao);
    }
}
