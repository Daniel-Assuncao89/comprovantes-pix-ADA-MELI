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

/** Os problemas do as-is provados em teste — cada um tem a correção na branch "gabarito". */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class EntidadeDeusTest {

    @Autowired
    private TestRestTemplate http;

    @Test
    void aRespostaParaOClienteVazaDadosDoAntifraude() {
        ResponseEntity<String> resposta = http.postForEntity("/comprovantes", json(COMPROVANTE), String.class);

        assertThat(resposta.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        assertThat(resposta.getBody()).contains("score_risco", "suspeito", "quantidade_consultas");
    }

    @Test
    void contaComSeisCaracteresPassaPorqueARegraEstaEspalhadaEErrada() {
        String contaComSeis = COMPROVANTE.replace("\"00276\"", "\"002761\"");

        ResponseEntity<String> resposta = http.postForEntity("/comprovantes", json(contaComSeis), String.class);

        assertThat(resposta.getStatusCode()).isEqualTo(HttpStatus.CREATED); // no gabarito: 400
    }

    @Test
    void cadaErroEhTratadoDeUmJeito() {
        String agenciaErrada = COMPROVANTE.replace("\"2022\"", "\"20\"");
        String contaErrada = COMPROVANTE.replace("\"00276\"", "\"0027600\"");

        assertThat(http.postForEntity("/comprovantes", json(agenciaErrada), String.class).getStatusCode())
                .isEqualTo(HttpStatus.BAD_REQUEST);            // validada no controller
        assertThat(http.postForEntity("/comprovantes", json(contaErrada), String.class).getStatusCode())
                .isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR);  // validada no service
    }
}
