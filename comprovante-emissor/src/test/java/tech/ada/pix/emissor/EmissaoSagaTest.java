package tech.ada.pix.emissor;

import tech.ada.pix.contracts.ComprovanteRequest;
import tech.ada.pix.contracts.TipoChavePix;
import tech.ada.pix.contracts.TipoDocumento;
import tech.ada.pix.emissor.dominio.Comprovante;
import tech.ada.pix.emissor.dominio.StatusComprovante;
import tech.ada.pix.emissor.gravacao.GravacaoAdapter;
import tech.ada.pix.emissor.gravacao.GravacaoFalhouException;
import tech.ada.pix.emissor.persistencia.ComprovanteRepository;
import org.junit.jupiter.api.Test;
import org.springframework.web.client.ResourceAccessException;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;

/** A SAGA "emitir → gravar": o caminho feliz e a compensação. */
class EmissaoSagaTest {

    static final ComprovanteRequest PIX = new ComprovanteRequest(
            "Giovanni", TipoDocumento.CPF, "50329291076", "2022", "00276", "0",
            new BigDecimal("23.99"), TipoChavePix.CELULAR, "11948755536",
            "Fernando", "churrasco", LocalDateTime.parse("2022-04-10T20:03:57"));

    private final ComprovanteRepository comprovantes = new ComprovanteRepository();
    private final GravacaoAdapter gravacao = mock(GravacaoAdapter.class);
    private final EmissaoService emissao = new EmissaoService(comprovantes, gravacao);

    @Test
    void quandoOGravadorGravaOComprovanteFicaGravado() {
        UUID id = emissao.aceitar(PIX).identificadorComprovante();

        assertThat(comprovantes.buscar(id)).get()
                .extracting(Comprovante::status).isEqualTo(StatusComprovante.GRAVADO);
    }

    @Test
    void quandoAGravacaoFalhaASagaCompensaInvalidandoOComprovante() {
        doThrow(new GravacaoFalhouException(UUID.randomUUID(), new ResourceAccessException("gravador fora")))
                .when(gravacao).gravar(any(), any());

        assertThatThrownBy(() -> emissao.aceitar(PIX)).isInstanceOf(GravacaoFalhouException.class);

        // o comprovante NÃO sumiu: ficou registrado como INVALIDADO, com o motivo (rastro)
        assertThat(comprovantes.todos()).singleElement().satisfies(c -> {
            assertThat(c.status()).isEqualTo(StatusComprovante.INVALIDADO);
            assertThat(c.motivoInvalidacao()).contains("gravador fora");
        });
    }
}
