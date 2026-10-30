package tech.ada.pix.contracts;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Evento publicado no tópico Kafka pelo gravador após persistir (Aula 6).
 * É um EVENTO (fato "isto aconteceu"), lido por vários consumer groups
 * (notificação, antifraude, BI) sem o gravador conhecê-los.
 */
public record ComprovanteGravadoEvent(
        UUID identificadorComprovante,
        LocalDateTime dataHoraGravacao
) {
}
