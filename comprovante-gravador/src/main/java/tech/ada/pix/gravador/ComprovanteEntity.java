package tech.ada.pix.gravador;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Persistência do comprovante no bounded context GRAVAÇÃO.
 * Base SEGREGADA deste serviço (não compartilhar tabela com outros serviços — Aula 1).
 */
@Entity
public class ComprovanteEntity {

    @Id
    private UUID id; // = identificadorComprovante (chave de idempotência)

    private String nome;
    private String numeroDocumento;
    private BigDecimal valorTransacao;
    private String chavePixDestino;
    private LocalDateTime dataHoraTransacao;
    private LocalDateTime dataHoraGravacao;

    protected ComprovanteEntity() {
        // exigido pelo JPA
    }

    public ComprovanteEntity(UUID id, String nome, String numeroDocumento, BigDecimal valorTransacao,
                             String chavePixDestino, LocalDateTime dataHoraTransacao) {
        this.id = id;
        this.nome = nome;
        this.numeroDocumento = numeroDocumento;
        this.valorTransacao = valorTransacao;
        this.chavePixDestino = chavePixDestino;
        this.dataHoraTransacao = dataHoraTransacao;
        this.dataHoraGravacao = LocalDateTime.now();
    }

    public UUID getId() {
        return id;
    }

    public LocalDateTime getDataHoraGravacao() {
        return dataHoraGravacao;
    }
}
