package cl.deliciasdelsur.backend.service;

import cl.deliciasdelsur.backend.dto.ProductoRequest;
import cl.deliciasdelsur.backend.exception.RecursoNoEncontradoException;
import cl.deliciasdelsur.backend.exception.ReglaNegocioException;
import cl.deliciasdelsur.backend.model.Categoria;
import cl.deliciasdelsur.backend.model.Producto;
import cl.deliciasdelsur.backend.repository.ProductoRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

// Lógica de negocio de productos: filtros, validación de categoría y borrado seguro
@Service
@RequiredArgsConstructor
public class ProductoService {

    private final ProductoRepository productoRepository;
    private final CategoriaService categoriaService;

    public List<Producto> listar(Long categoriaId, String nombre) {
        if (categoriaId != null) {
            return productoRepository.findByCategoriaId(categoriaId);
        }
        if (nombre != null && !nombre.isBlank()) {
            return productoRepository.findByNombreContainingIgnoreCase(nombre);
        }
        return productoRepository.findAll();
    }

    public Producto obtenerPorId(Long id) {
        return productoRepository.findById(id)
                .orElseThrow(() -> new RecursoNoEncontradoException("Producto no encontrado con id " + id));
    }

    @Transactional
    public Producto crear(ProductoRequest request) {
        Categoria categoria = categoriaService.obtenerPorId(request.getCategoriaId());
        Producto producto = Producto.builder()
                .nombre(request.getNombre())
                .descripcion(request.getDescripcion())
                .precio(request.getPrecio())
                .stock(request.getStock())
                .imagenUrl(request.getImagenUrl())
                .oferta(request.getOferta() == null ? 0 : request.getOferta())
                .categoria(categoria)
                .build();
        return productoRepository.save(producto);
    }

    @Transactional
    public Producto actualizar(Long id, ProductoRequest request) {
        Producto producto = obtenerPorId(id);
        Categoria categoria = categoriaService.obtenerPorId(request.getCategoriaId());
        producto.setNombre(request.getNombre());
        producto.setDescripcion(request.getDescripcion());
        producto.setPrecio(request.getPrecio());
        producto.setStock(request.getStock());
        producto.setImagenUrl(request.getImagenUrl());
        producto.setOferta(request.getOferta() == null ? 0 : request.getOferta());
        producto.setCategoria(categoria);
        return productoRepository.save(producto);
    }

    @Transactional
    public void eliminar(Long id) {
        Producto producto = obtenerPorId(id);
        try {
            productoRepository.delete(producto);
            productoRepository.flush();
        } catch (DataIntegrityViolationException e) {
            throw new ReglaNegocioException("No se puede eliminar un producto que ya tiene ventas registradas");
        }
    }
}