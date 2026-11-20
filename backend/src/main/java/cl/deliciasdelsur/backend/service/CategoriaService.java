package cl.deliciasdelsur.backend.service;

import cl.deliciasdelsur.backend.dto.CategoriaRequest;
import cl.deliciasdelsur.backend.exception.RecursoNoEncontradoException;
import cl.deliciasdelsur.backend.exception.ReglaNegocioException;
import cl.deliciasdelsur.backend.model.Categoria;
import cl.deliciasdelsur.backend.repository.CategoriaRepository;
import cl.deliciasdelsur.backend.repository.ProductoRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

// Lógica de negocio de categorías: nombre único y no eliminar si tiene productos
@Service
@RequiredArgsConstructor
public class CategoriaService {

    private final CategoriaRepository categoriaRepository;
    private final ProductoRepository productoRepository;

    public List<Categoria> listar() {
        return categoriaRepository.findAll();
    }

    public Categoria obtenerPorId(Long id) {
        return categoriaRepository.findById(id)
                .orElseThrow(() -> new RecursoNoEncontradoException("Categoría no encontrada con id " + id));
    }

    @Transactional
    public Categoria crear(CategoriaRequest request) {
        if (categoriaRepository.existsByNombre(request.getNombre())) {
            throw new ReglaNegocioException("Ya existe una categoría con ese nombre");
        }
        Categoria categoria = Categoria.builder()
                .nombre(request.getNombre())
                .descripcion(request.getDescripcion())
                .build();
        return categoriaRepository.save(categoria);
    }

    @Transactional
    public Categoria actualizar(Long id, CategoriaRequest request) {
        Categoria categoria = obtenerPorId(id);
        boolean cambioNombre = !categoria.getNombre().equals(request.getNombre());
        if (cambioNombre && categoriaRepository.existsByNombre(request.getNombre())) {
            throw new ReglaNegocioException("Ya existe una categoría con ese nombre");
        }
        categoria.setNombre(request.getNombre());
        categoria.setDescripcion(request.getDescripcion());
        return categoriaRepository.save(categoria);
    }

    @Transactional
    public void eliminar(Long id) {
        Categoria categoria = obtenerPorId(id);
        if (!productoRepository.findByCategoriaId(id).isEmpty()) {
            throw new ReglaNegocioException("No se puede eliminar una categoría que tiene productos");
        }
        categoriaRepository.delete(categoria);
    }
}