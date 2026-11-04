package tech.ada.pix.emissor.gravacao;

import tech.ada.pix.contracts.ComprovanteRequest;
import tech.ada.pix.contracts.GravarComprovanteCommand;
import tech.ada.pix.emissor.dominio.Comprovante;
import tech.ada.pix.emissor.gravacao.client.GravadorClient;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClientException;

/**
 * A "tomada" entre a emissão e a gravação.
 *
 * O EmissaoService só sabe "gravar"; não sabe que existe HTTP do outro lado.
 * Na Aula 5 este adapter passa a publicar numa fila, e o EmissaoService nem percebe.
 */
@Component
public class GravacaoAdapter {

    private final GravadorClient gravador;

    public GravacaoAdapter(GravadorClient gravador) {
        this.gravador = gravador;
    }

    public void gravar(Comprovante comprovante, ComprovanteRequest dados) {
        try {
            gravador.gravar(new GravarComprovanteCommand(comprovante.id(), dados));
        } catch (RestClientException falha) {
            throw new GravacaoFalhouException(comprovante.id(), falha);
        }
    }
}
