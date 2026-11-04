package tech.ada.pix.emissor.gravacao;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

import java.util.UUID;

/**
 * O passo "gravar" da SAGA não deu certo (gravador fora, com erro ou demorou demais).
 * Vira HTTP 503: "não consegui registrar agora, tente de novo". NUNCA 202:
 * responder 202 aqui criaria um comprovante fantasma.
 */
@ResponseStatus(HttpStatus.SERVICE_UNAVAILABLE)
public class GravacaoFalhouException extends RuntimeException {
    public GravacaoFalhouException(UUID id, Throwable causa) {
        super("não foi possível gravar o comprovante " + id, causa);
    }
}
