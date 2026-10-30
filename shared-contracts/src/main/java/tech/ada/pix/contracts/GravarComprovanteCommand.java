package tech.ada.pix.contracts;

import java.util.UUID;

/**
 * Comando publicado na fila pelo emissor e consumido pelo gravador (Aula 4/5).
 * É uma MENSAGEM (comando "grave isto"), não um evento.
 *
 * <p>{@code identificadorComprovante} é a chave de idempotência: reprocessar o mesmo
 * comando NÃO deve gravar dois comprovantes (Aula 5).</p>
 */
public record GravarComprovanteCommand(
        UUID identificadorComprovante,
        ComprovanteRequest dados
) {
}
