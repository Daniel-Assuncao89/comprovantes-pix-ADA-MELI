package tech.ada.pix.contracts;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Payload de emissão de comprovante PIX (corpo do POST /comprovantes).
 *
 * <p>Campos em snake_case no JSON — o mapeamento é feito pela configuração global do
 * Jackson nos serviços (PropertyNamingStrategy SNAKE_CASE), mantendo este contrato sem
 * dependências de serialização.</p>
 *
 * <p>Atenção: isto é só o "envelope" de dados que chega pela rede (DTO), não o modelo de
 * domínio. TODO (Aula 1): criar no emissor a entidade {@code Comprovante} e os Value Objects.
 * TODO (Aula 4/5): vira o corpo da mensagem publicada na fila de gravação.</p>
 */
public record ComprovanteRequest(
        String nome,
        TipoDocumento tipoDocumento,
        String numeroDocumento,
        String numeroAgencia,        // 4 caracteres
        String numeroConta,          // 5 caracteres
        String digitoVerificadorConta, // 1 caractere
        BigDecimal valorTransacao,
        TipoChavePix tipoChavePixDestino,
        String chavePixDestino,
        String nomeClienteDestino,
        String identificacaoPix,
        LocalDateTime dataHoraTransacao
) {
}
