package tech.ada.pix.monolito;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/comprovantes")
public class ComprovanteController {

    private final ComprovanteService service;

    public ComprovanteController(ComprovanteService service) {
        this.service = service;
    }

    /**
     * ❌ Recebe e devolve a ENTIDADE DE BANCO direto: a resposta para o cliente
     * leva junto o score do antifraude, o contador de consultas, tudo.
     * ❌ Responde 201 porque grava na hora, no meio do POST.
     */
    @PostMapping
    public ResponseEntity<Comprovante> emitir(@RequestBody Comprovante comprovante) {
        // ❌ regra espalhada: um pedaço da validação fica aqui no controller...
        if (comprovante.getNumeroAgencia() == null || comprovante.getNumeroAgencia().length() != 4)
            return ResponseEntity.badRequest().build();

        return ResponseEntity.status(HttpStatus.CREATED).body(service.emitir(comprovante));
    }

    @GetMapping("/{id}")
    public ResponseEntity<Comprovante> consultar(@PathVariable Long id) {
        return service.consultar(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }
}
