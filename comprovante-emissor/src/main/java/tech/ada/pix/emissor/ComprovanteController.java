package tech.ada.pix.emissor;

import tech.ada.pix.contracts.ComprovanteAceito;
import tech.ada.pix.contracts.ComprovanteRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Entrypoint do bounded context EMISSÃO.
 *
 * 202 → aceito e gravado · 400 → campo inválido · 503 → gravação falhou (SAGA compensada).
 */
@RestController
@RequestMapping("/comprovantes")
public class ComprovanteController {

    private final EmissaoService emissao;

    public ComprovanteController(EmissaoService emissao) {
        this.emissao = emissao;
    }

    @PostMapping
    public ResponseEntity<ComprovanteAceito> emitir(@RequestBody ComprovanteRequest request) {
        ComprovanteAceito aceito = emissao.aceitar(request);
        return ResponseEntity.status(HttpStatus.ACCEPTED).body(aceito);
    }
}
