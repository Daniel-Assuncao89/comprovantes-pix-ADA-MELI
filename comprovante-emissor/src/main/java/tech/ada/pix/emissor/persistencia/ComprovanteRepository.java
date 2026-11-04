package tech.ada.pix.emissor.persistencia;

import tech.ada.pix.emissor.dominio.Comprovante;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * A "base" do emissor: guarda os comprovantes e em que pé cada um está na SAGA.
 *
 * Fica em memória para a aula ficar simples. Em produção seria uma tabela na base
 * do emissor: o estado da SAGA precisa sobreviver a um restart do serviço.
 */
@Repository
public class ComprovanteRepository {

    private final Map<UUID, Comprovante> base = new ConcurrentHashMap<>();

    public void salvar(Comprovante comprovante) {
        base.put(comprovante.id(), comprovante);
    }

    public Optional<Comprovante> buscar(UUID id) {
        return Optional.ofNullable(base.get(id));
    }

    public List<Comprovante> todos() {
        return List.copyOf(base.values());
    }
}
