package tech.ada.pix.emissor.persistencia;

import tech.ada.pix.emissor.dominio.Comprovante;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * A base do emissor: guarda os comprovantes e em que pé cada um está na SAGA.
 *
 * Fala a língua do domínio (recebe e devolve {@link Comprovante}) e, por baixo,
 * usa a tabela (H2, {@code jdbc:h2:mem:emissor}).
 *
 * Cada {@code salvar(...)} é uma TRANSAÇÃO LOCAL, com o seu próprio COMMIT:
 * o {@code save} do Spring Data já é transacional. É isso que faz o ACEITO valer
 * antes de chamar o gravador, e o INVALIDADO (a compensação) ficar registrado.
 */
@Repository
public class ComprovanteRepository {

    private final ComprovanteJpaRepository tabela;

    public ComprovanteRepository(ComprovanteJpaRepository tabela) {
        this.tabela = tabela;
    }

    public void salvar(Comprovante comprovante) {
        tabela.save(ComprovanteEntity.de(comprovante));
    }

    public Optional<Comprovante> buscar(UUID id) {
        return tabela.findById(id).map(ComprovanteEntity::paraDominio);
    }

    public List<Comprovante> todos() {
        return tabela.findAll().stream().map(ComprovanteEntity::paraDominio).toList();
    }
}
