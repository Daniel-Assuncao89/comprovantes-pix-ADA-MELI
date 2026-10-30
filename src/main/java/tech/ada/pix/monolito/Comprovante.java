package tech.ada.pix.monolito;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;

import java.time.LocalDateTime;

/**
 * ❌ "ENTIDADE DEUS": uma classe só com os dados de TODAS as áreas.
 *
 * Emissão, consulta, notificação e antifraude usam a mesma classe e a mesma tabela.
 * Qualquer mudança de uma área mexe no código de todas.
 *
 * ❌ E é ANÊMICA: só getters e setters. Nenhuma regra mora aqui —
 * qualquer um pode colocar qualquer valor em qualquer campo.
 *
 * 💡 Repare nos blocos de campos: cada bloco é um bounded context escondido.
 */
@Entity
public class Comprovante {

    @Id
    @GeneratedValue
    private Long id;

    // --- dados da EMISSÃO (tudo String e double soltos: "obsessão por primitivos")
    private String nome;
    private String tipoDocumento;
    private String numeroDocumento;
    private String numeroAgencia;
    private String numeroConta;
    private String digitoVerificadorConta;
    private double valorTransacao;          // ❌ double para dinheiro não é exato
    private String tipoChavePixDestino;
    private String chavePixDestino;
    private String nomeClienteDestino;
    private String identificacaoPix;
    private LocalDateTime dataHoraTransacao;
    private String status;                  // ❌ texto livre: aceita qualquer coisa

    // --- dados da CONSULTA
    private int quantidadeConsultas;
    private LocalDateTime ultimaConsulta;

    // --- dados da NOTIFICAÇÃO
    private boolean notificacaoEnviada;

    // --- dados do ANTIFRAUDE
    private Integer scoreRisco;
    private boolean suspeito;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getNome() { return nome; }
    public void setNome(String nome) { this.nome = nome; }
    public String getTipoDocumento() { return tipoDocumento; }
    public void setTipoDocumento(String tipoDocumento) { this.tipoDocumento = tipoDocumento; }
    public String getNumeroDocumento() { return numeroDocumento; }
    public void setNumeroDocumento(String numeroDocumento) { this.numeroDocumento = numeroDocumento; }
    public String getNumeroAgencia() { return numeroAgencia; }
    public void setNumeroAgencia(String numeroAgencia) { this.numeroAgencia = numeroAgencia; }
    public String getNumeroConta() { return numeroConta; }
    public void setNumeroConta(String numeroConta) { this.numeroConta = numeroConta; }
    public String getDigitoVerificadorConta() { return digitoVerificadorConta; }
    public void setDigitoVerificadorConta(String digitoVerificadorConta) { this.digitoVerificadorConta = digitoVerificadorConta; }
    public double getValorTransacao() { return valorTransacao; }
    public void setValorTransacao(double valorTransacao) { this.valorTransacao = valorTransacao; }
    public String getTipoChavePixDestino() { return tipoChavePixDestino; }
    public void setTipoChavePixDestino(String tipoChavePixDestino) { this.tipoChavePixDestino = tipoChavePixDestino; }
    public String getChavePixDestino() { return chavePixDestino; }
    public void setChavePixDestino(String chavePixDestino) { this.chavePixDestino = chavePixDestino; }
    public String getNomeClienteDestino() { return nomeClienteDestino; }
    public void setNomeClienteDestino(String nomeClienteDestino) { this.nomeClienteDestino = nomeClienteDestino; }
    public String getIdentificacaoPix() { return identificacaoPix; }
    public void setIdentificacaoPix(String identificacaoPix) { this.identificacaoPix = identificacaoPix; }
    public LocalDateTime getDataHoraTransacao() { return dataHoraTransacao; }
    public void setDataHoraTransacao(LocalDateTime dataHoraTransacao) { this.dataHoraTransacao = dataHoraTransacao; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public int getQuantidadeConsultas() { return quantidadeConsultas; }
    public void setQuantidadeConsultas(int quantidadeConsultas) { this.quantidadeConsultas = quantidadeConsultas; }
    public LocalDateTime getUltimaConsulta() { return ultimaConsulta; }
    public void setUltimaConsulta(LocalDateTime ultimaConsulta) { this.ultimaConsulta = ultimaConsulta; }
    public boolean isNotificacaoEnviada() { return notificacaoEnviada; }
    public void setNotificacaoEnviada(boolean notificacaoEnviada) { this.notificacaoEnviada = notificacaoEnviada; }
    public Integer getScoreRisco() { return scoreRisco; }
    public void setScoreRisco(Integer scoreRisco) { this.scoreRisco = scoreRisco; }
    public boolean isSuspeito() { return suspeito; }
    public void setSuspeito(boolean suspeito) { this.suspeito = suspeito; }
}
