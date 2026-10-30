package tech.ada.pix.emissor;

import tech.ada.pix.contracts.ComprovanteAceito;
import tech.ada.pix.contracts.ComprovanteRequest;
import tech.ada.pix.emissor.dominio.ChavePix;
import tech.ada.pix.emissor.dominio.Comprovante;
import tech.ada.pix.emissor.dominio.ContaBancaria;
import tech.ada.pix.emissor.dominio.Dinheiro;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

/**
 * Serviço de APLICAÇÃO do bounded context EMISSÃO (Aula 1 — DDD).
 *
 * Ele só coordena: transforma o envelope (ComprovanteRequest) no modelo de
 * domínio (Comprovante + Value Objects). As regras moram nos objetos de domínio.
 * A gravação ainda NÃO acontece aqui — será orquestrada na Aula 2 (SAGA) e
 * desacoplada por mensageria na Aula 4.
 */
@Service
public class EmissaoService {

    public ComprovanteAceito aceitar(ComprovanteRequest r) {
        validarCamposRestantes(r);
        Comprovante comprovante = Comprovante.emitir(
                r.nome(),
                new ContaBancaria(r.numeroAgencia(), r.numeroConta(), r.digitoVerificadorConta()),
                new Dinheiro(r.valorTransacao()),
                new ChavePix(r.tipoChavePixDestino(), r.chavePixDestino()));
        return new ComprovanteAceito(comprovante.id(), LocalDateTime.now());
    }

    /** Campos que ainda não viraram Value Object (próximo candidato: Documento = tipo + número). */
    private void validarCamposRestantes(ComprovanteRequest r) {
        if (r.tipoDocumento() == null) throw new ComprovanteInvalidoException("tipo_documento é obrigatório");
        exigir(r.numeroDocumento(), "numero_documento");
        exigir(r.nomeClienteDestino(), "nome_cliente_destino");
        if (r.dataHoraTransacao() == null) throw new ComprovanteInvalidoException("data_hora_transacao é obrigatória");
    }

    private void exigir(String valor, String campo) {
        if (valor == null || valor.isBlank())
            throw new ComprovanteInvalidoException(campo + " é obrigatório");
    }
}
