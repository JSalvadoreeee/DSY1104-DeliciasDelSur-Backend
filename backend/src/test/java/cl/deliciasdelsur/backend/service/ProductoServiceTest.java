package cl.deliciasdelsur.backend.service;

import cl.deliciasdelsur.backend.dto.ProductoRequest;
import cl.deliciasdelsur.backend.exception.RecursoNoEncontradoException;
import cl.deliciasdelsur.backend.exception.ReglaNegocioException;
import cl.deliciasdelsur.backend.model.Categoria;
import cl.deliciasdelsur.backend.model.Producto;
import cl.deliciasdelsur.backend.repository.ProductoRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.DataIntegrityViolationException;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

// Pruebas de la lógica de productos: oferta por defecto, filtros, categoría válida y borrado seguro
@ExtendWith(MockitoExtension.class)
class ProductoServiceTest {

    @Mock
    private ProductoRepository productoRepository;
    @Mock
    private CategoriaService categoriaService;

    @InjectMocks
    private ProductoService productoService;

    private Categoria categoria;
    private Producto producto;

    @BeforeEach
    void preparar() {
        categoria = Categoria.builder().id(1L).nombre("Conservas").build();
        producto = Producto.builder().id(1L).nombre("Mermelada").precio(2500).stock(10).oferta(0).categoria(categoria).build();
    }

    private ProductoRequest solicitud(Integer precio, Integer oferta, Long categoriaId) {
        ProductoRequest request = new ProductoRequest();
        request.setNombre("Pickles de Pepino");
        request.setDescripcion("Pepinillos en conserva");
        request.setPrecio(precio);
        request.setStock(20);
        request.setOferta(oferta);
        request.setCategoriaId(categoriaId);
        return request;
    }

    @Test
    void crearSinOfertaGuardaOfertaEnCero() {
        when(categoriaService.obtenerPorId(1L)).thenReturn(categoria);
        when(productoRepository.save(any(Producto.class))).thenAnswer(inv -> inv.getArgument(0));

        Producto creado = productoService.crear(solicitud(2000, null, 1L));

        assertEquals(0, creado.getOferta());
        assertEquals(categoria, creado.getCategoria());
        assertEquals(2000, creado.getPrecio());
    }

    @Test
    void crearConCategoriaInexistenteLanzaExcepcionYNoGuarda() {
        when(categoriaService.obtenerPorId(99L)).thenThrow(new RecursoNoEncontradoException("Categoría no encontrada"));

        assertThrows(RecursoNoEncontradoException.class, () -> productoService.crear(solicitud(2000, 0, 99L)));
        verify(productoRepository, never()).save(any());
    }

    @Test
    void listarConCategoriaFiltraPorCategoria() {
        when(productoRepository.findByCategoriaId(1L)).thenReturn(List.of(producto));

        assertEquals(1, productoService.listar(1L, null).size());
    }

    @Test
    void listarConNombreBuscaPorNombre() {
        when(productoRepository.findByNombreContainingIgnoreCase("merme")).thenReturn(List.of(producto));

        assertEquals(1, productoService.listar(null, "merme").size());
    }

    @Test
    void actualizarCambiaPrecioYOferta() {
        when(productoRepository.findById(1L)).thenReturn(Optional.of(producto));
        when(categoriaService.obtenerPorId(1L)).thenReturn(categoria);
        when(productoRepository.save(any(Producto.class))).thenAnswer(inv -> inv.getArgument(0));

        Producto actualizado = productoService.actualizar(1L, solicitud(3000, 20, 1L));

        assertEquals(3000, actualizado.getPrecio());
        assertEquals(20, actualizado.getOferta());
    }

    @Test
    void eliminarProductoConVentasLanzaReglaDeNegocio() {
        when(productoRepository.findById(1L)).thenReturn(Optional.of(producto));
        doThrow(new DataIntegrityViolationException("tiene ventas")).when(productoRepository).delete(producto);

        assertThrows(ReglaNegocioException.class, () -> productoService.eliminar(1L));
    }

    @Test
    void obtenerProductoInexistenteLanzaExcepcion() {
        when(productoRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(RecursoNoEncontradoException.class, () -> productoService.obtenerPorId(99L));
    }
}