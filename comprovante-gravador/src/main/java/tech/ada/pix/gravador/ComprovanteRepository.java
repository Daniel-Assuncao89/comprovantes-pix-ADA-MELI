package tech.ada.pix.gravador;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface ComprovanteRepository extends JpaRepository<ComprovanteEntity, UUID> {
}
