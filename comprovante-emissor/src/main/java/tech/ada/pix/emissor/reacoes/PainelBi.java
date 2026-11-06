package tech.ada.pix.emissor.reacoes;

import tech.ada.pix.emissor.dominio.ComprovanteGravado;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

/**
 * Reação ao ComprovanteGravado (Aula 3): conta o PIX no painel de BI.
 *
 * É a reação que a turma cria AO VIVO, para ver a regra do evento na prática:
 * entrou um interessado novo e o EmissaoService não mudou nenhuma linha.
 */
@Component
public class PainelBi {

    private static final Logger log = LoggerFactory.getLogger(PainelBi.class);

    @Async
    @EventListener
    public void quandoGravado(ComprovanteGravado evento) {
        log.info("BI: +1 PIX no painel (comprovante {})", evento.idComprovante());
    }
}
