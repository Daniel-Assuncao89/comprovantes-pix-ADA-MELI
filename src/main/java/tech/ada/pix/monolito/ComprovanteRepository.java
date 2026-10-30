package tech.ada.pix.monolito;

import org.springframework.data.jpa.repository.JpaRepository;

/** ❌ Um repositório, uma tabela, um banco — para emissão, consulta, notificação e antifraude. */
public interface ComprovanteRepository extends JpaRepository<Comprovante, Long> {
}
