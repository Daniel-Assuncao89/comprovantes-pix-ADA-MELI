package tech.ada.pix.emissor;

import tech.ada.pix.contracts.ComprovanteRequest;
import tech.ada.pix.contracts.TipoChavePix;
import tech.ada.pix.contracts.TipoDocumento;
import tech.ada.pix.emissor.dominio.ChavePix;
import tech.ada.pix.emissor.dominio.ContaBancaria;
import tech.ada.pix.emissor.dominio.Dinheiro;
import tech.ada.pix.emissor.dominio.StatusComprovante;
import tech.ada.pix.emissor.gravacao.GravacaoAdapter;
import tech.ada.pix.emissor.gravacao.GravacaoFalhouException;
import tech.ada.pix.emissor.persistencia.ComprovanteJpaRepository;
import tech.ada.pix.emissor.persistencia.ComprovanteRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.web.client.ResourceAccessException;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;

/**
 * A SAGA "emitir → gravar": o caminho feliz e a compensação.
 *
 * Usa a base de verdade do emissor (H2). Só o gravador é de mentira (Mockito).
 * O que se confere é o que FICOU NO BANCO, depois do COMMIT de cada passo.
 */
@SpringBootTest
class EmissaoSagaTest {

    static final ComprovanteRequest PIX = new ComprovanteRequest(
            "Giovanni", TipoDocumento.CPF, "50329291076", "2022", "00276", "0",
            new BigDecimal("23.99"), TipoChavePix.CELULAR, "11948755536",
            "Fernando", "churrasco", LocalDateTime.parse("2022-04-10T20:03:57"));

    @Autowired
    private EmissaoService emissao;

    @Autowired
    private ComprovanteRepository comprovantes;

    @Autowired
    private ComprovanteJpaRepository tabela;

    @MockBean
    private GravacaoAdapter gravacao;   // o gravador de mentira: por padrão, "grava" sem erro

    @BeforeEach
    void limparABase() {
        tabela.deleteAll();
    }

    @Test
    void quandoOGravadorGravaOComprovanteFicaGravado() {
        UUID id = emissao.aceitar(PIX).identificadorComprovante();

        // o que voltou do banco é o mesmo comprovante, com os Value Objects inteiros
        assertThat(comprovantes.buscar(id)).get().satisfies(c -> {
            assertThat(c.status()).isEqualTo(StatusComprovante.GRAVADO);
            assertThat(c.contaOrigem()).isEqualTo(new ContaBancaria("2022", "00276", "0"));
            assertThat(c.valor()).isEqualTo(new Dinheiro(new BigDecimal("23.99")));
            assertThat(c.chaveDestino()).isEqualTo(new ChavePix(TipoChavePix.CELULAR, "11948755536"));
        });
    }

    @Test
    void quandoAGravacaoFalhaASagaCompensaInvalidandoOComprovante() {
        doThrow(new GravacaoFalhouException(UUID.randomUUID(), new ResourceAccessException("gravador fora")))
                .when(gravacao).gravar(any(), any());

        assertThatThrownBy(() -> emissao.aceitar(PIX)).isInstanceOf(GravacaoFalhouException.class);

        // o comprovante NÃO sumiu: ficou no banco como INVALIDADO, com o motivo (rastro).
        // Se alguém puser @Transactional no aceitar(), este teste quebra: o ROLLBACK levaria o rastro junto.
        assertThat(comprovantes.todos()).singleElement().satisfies(c -> {
            assertThat(c.status()).isEqualTo(StatusComprovante.INVALIDADO);
            assertThat(c.motivoInvalidacao()).contains("gravador fora");
        });
    }
}
