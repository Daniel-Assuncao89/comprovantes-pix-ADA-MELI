package tech.ada.pix.emissor.reacoes;

import tech.ada.pix.emissor.dominio.ComprovanteGravado;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

/**
 * Reação ao ComprovanteGravado (Aula 3): o antifraude olha o PIX depois que ele aconteceu.
 * Usa o valor que veio DENTRO do evento: não precisa perguntar nada ao emissor.
 */
@Component
public class Antifraude {

    private static final Logger log = LoggerFactory.getLogger(Antifraude.class);

    @Async
    @EventListener
    public void quandoGravado(ComprovanteGravado evento) {
        log.info("Antifraude: analisando o comprovante {} (R$ {})",
                evento.idComprovante(), evento.valor().valor());
    }
}
