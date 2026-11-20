package cl.deliciasdelsur.backend.repository;

import cl.deliciasdelsur.backend.model.Boleta;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface BoletaRepository extends JpaRepository<Boleta, Long> {
    List<Boleta> findByUsuarioIdOrderByFechaDesc(Long usuarioId);
    List<Boleta> findByUsuarioCorreoOrderByFechaDesc(String correo);
    List<Boleta> findAllByOrderByFechaDesc();
}