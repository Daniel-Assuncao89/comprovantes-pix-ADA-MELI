package tech.ada.pix.monolito;

import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;

/** O mesmo comprovante usado no gabarito, para comparar os dois lados. */
final class Exemplo {

    static final String COMPROVANTE = """
            {"nome":"Giovanni","tipo_documento":"CPF","numero_documento":"50329291076",
             "numero_agencia":"2022","numero_conta":"00276","digito_verificador_conta":"0",
             "valor_transacao":23.99,"tipo_chave_pix_destino":"CELULAR","chave_pix_destino":"11948755536",
             "nome_cliente_destino":"Fernando","identificacao_pix":"churrasco",
             "data_hora_transacao":"2022-04-10T20:03:57"}
            """;

    static HttpEntity<String> json(String corpo) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        return new HttpEntity<>(corpo, headers);
    }

    private Exemplo() {
    }
}
