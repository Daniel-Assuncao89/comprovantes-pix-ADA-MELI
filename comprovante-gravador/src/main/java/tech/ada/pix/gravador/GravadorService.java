package tech.ada.pix.gravador;

import tech.ada.pix.contracts.GravarComprovanteCommand;
import org.springframework.stereotype.Service;

/**
 * Núcleo do bounded context GRAVAÇÃO.
 * Aula 1: capacidade de persistir já pronta (ainda não há quem invoque — vem na Aula 2).
 */
@Service
public class GravadorService {

    private final ComprovanteRepository repository;

    public GravadorService(ComprovanteRepository repository) {
        this.repository = repository;
    }

    public void gravar(GravarComprovanteCommand command) {
        ComprovanteEntity entidade = ComprovanteEntity.de(
                command.identificadorComprovante(), command.dados());
        repository.save(entidade);
    }
}
