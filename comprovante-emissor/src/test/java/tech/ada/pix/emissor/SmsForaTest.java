package tech.ada.pix.emissor;

import tech.ada.pix.emissor.dominio.StatusComprovante;
import tech.ada.pix.emissor.gravacao.GravacaoAdapter;
import tech.ada.pix.emissor.persistencia.ComprovanteRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;

import java.util.UUID;

import static tech.ada.pix.emissor.EmissaoSagaTest.PIX;
import static org.assertj.core.api.Assertions.assertThat;

/**
 * Aula 3: a "operadora de SMS" está fora (demo.sms-fora=true), e o PIX tem que dar certo mesmo assim.
 *
 * Quem garante é o @Async da NotificacaoSms: a reação roda em outra thread, e a falha dela
 * não volta para o emissor. Se alguém tirar o @Async, este teste quebra.
 */
@SpringBootTest(properties = "demo.sms-fora=true")
class SmsForaTest {

    @Autowired
    private EmissaoService emissao;

    @Autowired
    private ComprovanteRepository comprovantes;

    @MockBean
    private GravacaoAdapter gravacao;   // o gravador de mentira: por padrão, "grava" sem erro

    @Test
    void comOSmsForaOPixContinuaGravado() {
        UUID id = emissao.aceitar(PIX).identificadorComprovante();   // não lança exceção

        assertThat(comprovantes.buscar(id)).get()
                .satisfies(c -> assertThat(c.status()).isEqualTo(StatusComprovante.GRAVADO));
    }
}
