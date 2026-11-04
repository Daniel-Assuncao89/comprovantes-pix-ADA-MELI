package tech.ada.pix.emissor.gravacao.client;

import tech.ada.pix.contracts.GravarComprovanteCommand;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

/**
 * Cliente HTTP do gravador: só sabe fazer a chamada. Quem decide o que fazer
 * com o erro é o GravacaoAdapter.
 */
@Component
public class GravadorClient {

    private final RestClient http;

    public GravadorClient(RestClient.Builder builder,
                          @Value("${gravador.base-url}") String baseUrl,
                          @Value("${gravador.timeout-ms}") int timeoutMs) {
        SimpleClientHttpRequestFactory tempoLimite = new SimpleClientHttpRequestFactory();
        tempoLimite.setConnectTimeout(timeoutMs);
        tempoLimite.setReadTimeout(timeoutMs);
        // O builder do Spring já vem com o Jackson do serviço (JSON em snake_case).
        this.http = builder.baseUrl(baseUrl).requestFactory(tempoLimite).build();
    }

    /** Lança RestClientException se o gravador estiver fora, responder erro ou estourar o tempo. */
    public void gravar(GravarComprovanteCommand comando) {
        http.post().uri("/interno/comprovantes").body(comando).retrieve().toBodilessEntity();
    }
}
