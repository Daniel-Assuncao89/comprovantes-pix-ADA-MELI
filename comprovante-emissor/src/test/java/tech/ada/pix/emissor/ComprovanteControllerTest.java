package tech.ada.pix.emissor;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/** O POST /comprovantes de ponta a ponta: 202 quando está certo, 400 quando não está. */
@WebMvcTest(ComprovanteController.class)
@Import(EmissaoService.class)
class ComprovanteControllerTest {

    private static final String COMPROVANTE_VALIDO = """
            {"nome":"Giovanni","tipo_documento":"CPF","numero_documento":"50329291076",
             "numero_agencia":"2022","numero_conta":"00276","digito_verificador_conta":"0",
             "valor_transacao":23.99,"tipo_chave_pix_destino":"CELULAR","chave_pix_destino":"11948755536",
             "nome_cliente_destino":"Fernando","identificacao_pix":"churrasco",
             "data_hora_transacao":"2022-04-10T20:03:57"}
            """;

    @Autowired
    private MockMvc mvc;

    @Test
    void comprovanteValidoEhAceitoCom202() throws Exception {
        mvc.perform(post("/comprovantes").contentType(MediaType.APPLICATION_JSON).content(COMPROVANTE_VALIDO))
                .andExpect(status().isAccepted())
                .andExpect(jsonPath("$.identificador_comprovante").exists());
    }

    @Test
    void contaComSeisCaracteresEhRejeitadaCom400() throws Exception {
        String invalido = COMPROVANTE_VALIDO.replace("\"00276\"", "\"002761\"");

        mvc.perform(post("/comprovantes").contentType(MediaType.APPLICATION_JSON).content(invalido))
                .andExpect(status().isBadRequest());
    }
}
