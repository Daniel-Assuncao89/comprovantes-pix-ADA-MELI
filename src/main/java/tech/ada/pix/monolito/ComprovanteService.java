package tech.ada.pix.monolito;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Optional;

/**
 * ❌ "SERVIÇO DEUS": emissão, gravação, consulta, antifraude e notificação
 * no mesmo lugar — e na MESMA TRANSAÇÃO.
 *
 * Se qualquer parte falha, tudo falha junto. Se o SMS cair, o cliente
 * fica sem o comprovante (teste: notificacao.gateway-fora=true).
 */
@Service
public class ComprovanteService {

    private final ComprovanteRepository repository;
    private final boolean gatewaySmsFora;

    public ComprovanteService(ComprovanteRepository repository,
                              @Value("${notificacao.gateway-fora:false}") boolean gatewaySmsFora) {
        this.repository = repository;
        this.gatewaySmsFora = gatewaySmsFora;
    }

    @Transactional
    public Comprovante emitir(Comprovante comprovante) {
        // ❌ ...e outro pedaço da validação fica aqui. Com a regra ERRADA:
        //    aceita conta com 6 caracteres (o certo é 5). E erro aqui vira 500, não 400.
        if (comprovante.getNumeroConta() == null || comprovante.getNumeroConta().length() > 6)
            throw new IllegalArgumentException("conta inválida");

        comprovante.setStatus("GRAVADO");
        repository.save(comprovante);   // ❌ grava na hora: no pico, o cliente espera o banco

        analisarFraude(comprovante);    // ❌ antifraude dentro da emissão
        notificarCliente(comprovante);  // ❌ SMS dentro da emissão
        return comprovante;
    }

    public Optional<Comprovante> consultar(Long id) {
        // ❌ toda consulta vai no banco E ainda ESCREVE nele (contador de consultas)
        Optional<Comprovante> encontrado = repository.findById(id);
        encontrado.ifPresent(c -> {
            c.setQuantidadeConsultas(c.getQuantidadeConsultas() + 1);
            c.setUltimaConsulta(LocalDateTime.now());
            repository.save(c);
        });
        return encontrado;
    }

    private void analisarFraude(Comprovante comprovante) {
        int score = comprovante.getValorTransacao() > 5000 ? 90 : 10;
        comprovante.setScoreRisco(score);
        comprovante.setSuspeito(score > 50);
    }

    private void notificarCliente(Comprovante comprovante) {
        if (gatewaySmsFora)
            throw new IllegalStateException("gateway de SMS fora do ar");
        System.out.println("SMS enviado para " + comprovante.getNome());
        comprovante.setNotificacaoEnviada(true);
    }
}
