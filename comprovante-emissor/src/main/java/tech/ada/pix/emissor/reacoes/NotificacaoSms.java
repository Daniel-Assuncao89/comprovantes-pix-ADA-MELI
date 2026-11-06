package tech.ada.pix.emissor.reacoes;

import tech.ada.pix.emissor.dominio.ComprovanteGravado;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

/**
 * Reação ao ComprovanteGravado (Aula 3): avisa o cliente por SMS. Aqui é só um log.
 *
 * O @Async faz a reação rodar em OUTRA thread: se o SMS falhar, o PIX não falha junto.
 * Para ver o contrário, tire o @Async e suba o emissor com --demo.sms-fora=true.
 *
 * ⚠️ As reações moram no emissor só por enquanto: sem um broker, o evento não sai do processo.
 * Na aula de Kafka, cada reação vira um consumidor, num serviço próprio.
 */
@Component
public class NotificacaoSms {

    private static final Logger log = LoggerFactory.getLogger(NotificacaoSms.class);

    private final boolean smsFora;

    public NotificacaoSms(@Value("${demo.sms-fora:false}") boolean smsFora) {
        this.smsFora = smsFora;
    }

    @Async
    @EventListener
    public void quandoGravado(ComprovanteGravado evento) {
        if (smsFora) throw new IllegalStateException("operadora de SMS fora do ar"); // ⚠️ só para a demo
        log.info("SMS enviado: seu PIX de R$ {} foi concluído (comprovante {})",
                evento.valor().valor(), evento.idComprovante());
    }
}
