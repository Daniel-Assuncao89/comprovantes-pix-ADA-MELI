package tech.ada.pix.monolito;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import static tech.ada.pix.monolito.Exemplo.COMPROVANTE;
import static tech.ada.pix.monolito.Exemplo.json;
import static org.assertj.core.api.Assertions.assertThat;

/** A dor principal do monólito: tudo na mesma transação, então uma falha derruba tudo. */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
        properties = "notificacao.gateway-fora=true")
class AcoplamentoTest {

    @Autowired
    private TestRestTemplate http;

    @Autowired
    private ComprovanteRepository repository;

    @Test
    void quandoOSmsCaiOComprovanteNemEhSalvo() {
        long antes = repository.count();

        ResponseEntity<String> resposta = http.postForEntity("/comprovantes", json(COMPROVANTE), String.class);

        assertThat(resposta.getStatusCode()).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR);
        assertThat(repository.count()).isEqualTo(antes); // o comprovante se perdeu por causa do SMS
    }
}
