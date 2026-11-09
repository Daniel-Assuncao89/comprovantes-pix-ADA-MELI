package tech.ada.pix.emissor.reacoes;

import tech.ada.pix.emissor.dominio.ComprovanteGravado;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

/**
 * Reação ao ComprovanteGravado (exercício da Aula 3): o negócio pediu cashback em todo PIX.
 * Aqui é só um log.
 *
 * Repare: para o cashback existir, ninguém mexeu no EmissaoService. Ele continua publicando
 * o mesmo evento, sem saber quem escuta. Esta classe é só mais um interessado.
 */
@Component
public class Cashback {

    private static final Logger log = LoggerFactory.getLogger(Cashback.class);

    @Async
    @EventListener
    public void quandoGravado(ComprovanteGravado evento) {
        log.info("Cashback: 1% de R$ {} vai voltar para o cliente (comprovante {})",
                evento.valor().valor(), evento.idComprovante());
    }
}
