package tech.ada.pix.gravador;

import tech.ada.pix.contracts.GravarComprovanteCommand;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * A "porta da frente" do GRAVADOR (Aula 2). Ninguém escreve direto no banco dele:
 * quem quiser gravar, pede por aqui.
 *
 * 201 → gravou agora · 200 → já estava gravado (idempotência).
 * Na Aula 5 este passo deixa de ser REST e vira consumo de fila.
 */
@RestController
@RequestMapping("/interno/comprovantes")
public class GravadorController {

    private final GravadorService gravador;
    private final long atrasoMs;

    public GravadorController(GravadorService gravador, @Value("${demo.atraso-ms:0}") long atrasoMs) {
        this.gravador = gravador;
        this.atrasoMs = atrasoMs;
    }

    @PostMapping
    public ResponseEntity<Void> gravar(@RequestBody GravarComprovanteCommand command) throws InterruptedException {
        boolean gravouAgora = gravador.gravar(command);
        Thread.sleep(atrasoMs); // ⚠️ só para a demo
        return ResponseEntity.status(gravouAgora ? HttpStatus.CREATED : HttpStatus.OK).build();
    }
}
