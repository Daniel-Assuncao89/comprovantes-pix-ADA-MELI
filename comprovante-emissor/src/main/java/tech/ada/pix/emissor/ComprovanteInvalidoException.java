package tech.ada.pix.emissor;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

/** Lançada quando o comprovante recebido viola as regras obrigatórias → HTTP 400. */
@ResponseStatus(HttpStatus.BAD_REQUEST)
public class ComprovanteInvalidoException extends RuntimeException {
    public ComprovanteInvalidoException(String motivo) {
        super(motivo);
    }
}
