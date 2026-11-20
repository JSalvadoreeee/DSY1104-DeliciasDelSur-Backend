package cl.deliciasdelsur.backend.repository;

import cl.deliciasdelsur.backend.model.Producto;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface ProductoRepository extends JpaRepository<Producto, Long> {
    List<Producto> findByCategoriaId(Long categoriaId);
    List<Producto> findByNombreContainingIgnoreCase(String nombre);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select p from Producto p where p.id = :id")
    Optional<Producto> buscarParaActualizar(@Param("id") Long id);
}