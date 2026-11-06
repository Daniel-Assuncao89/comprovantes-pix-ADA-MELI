package tech.ada.pix.emissor;

import tech.ada.pix.emissor.dominio.ComprovanteGravado;
import tech.ada.pix.emissor.dominio.Dinheiro;
import tech.ada.pix.emissor.gravacao.GravacaoAdapter;
import tech.ada.pix.emissor.gravacao.GravacaoFalhouException;
import tech.ada.pix.emissor.persistencia.ComprovanteJpaRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.event.ApplicationEvents;
import org.springframework.test.context.event.RecordApplicationEvents;
import org.springframework.web.client.ResourceAccessException;

import java.math.BigDecimal;
import java.util.UUID;

import static tech.ada.pix.emissor.EmissaoSagaTest.PIX;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;

/** Aula 3: quando a SAGA termina, o emissor AVISA (evento). Quando ela compensa, não avisa nada. */
@SpringBootTest
@RecordApplicationEvents   // guarda os eventos publicados durante cada teste
class EventoComprovanteGravadoTest {

    @Autowired
    private EmissaoService emissao;

    @Autowired
    private ComprovanteJpaRepository tabela;

    @Autowired
    private ApplicationEvents eventos;

    @MockBean
    private GravacaoAdapter gravacao;   // o gravador de mentira: por padrão, "grava" sem erro

    @BeforeEach
    void limparABase() {
        tabela.deleteAll();
    }

    @Test
    void quandoASagaTerminaOEmissorAvisaUmaVez() {
        UUID id = emissao.aceitar(PIX).identificadorComprovante();

        assertThat(eventos.stream(ComprovanteGravado.class)).singleElement().satisfies(evento -> {
            assertThat(evento.idComprovante()).isEqualTo(id);
            assertThat(evento.valor()).isEqualTo(new Dinheiro(new BigDecimal("23.99")));   // o valor vai junto
        });
    }

    @Test
    void quandoASagaCompensaNinguemEhAvisado() {
        doThrow(new GravacaoFalhouException(UUID.randomUUID(), new ResourceAccessException("gravador fora")))
                .when(gravacao).gravar(any(), any());

        assertThatThrownBy(() -> emissao.aceitar(PIX)).isInstanceOf(GravacaoFalhouException.class);

        // não pode sair SMS de um PIX que foi desfeito
        assertThat(eventos.stream(ComprovanteGravado.class)).isEmpty();
    }
}
