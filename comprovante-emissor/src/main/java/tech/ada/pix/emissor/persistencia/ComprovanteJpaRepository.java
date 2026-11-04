package tech.ada.pix.emissor.persistencia;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

/** O acesso "cru" à tabela, feito pelo Spring Data. O resto do emissor usa o {@link ComprovanteRepository}. */
public interface ComprovanteJpaRepository extends JpaRepository<ComprovanteEntity, UUID> {
}
