package tech.ada.pix.contracts;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Resposta do POST /comprovantes (HTTP 202 Accepted).
 * O comprovante ainda NÃO foi gravado — foi aceito e enfileirado.
 */
public record ComprovanteAceito(
        UUID identificadorComprovante,
        LocalDateTime dataHoraRequisicao
) {
}
