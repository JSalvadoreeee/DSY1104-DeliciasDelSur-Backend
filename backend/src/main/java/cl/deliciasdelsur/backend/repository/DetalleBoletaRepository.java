package cl.deliciasdelsur.backend.repository;

import cl.deliciasdelsur.backend.model.DetalleBoleta;
import org.springframework.data.jpa.repository.JpaRepository;

public interface DetalleBoletaRepository extends JpaRepository<DetalleBoleta, Long> {
}