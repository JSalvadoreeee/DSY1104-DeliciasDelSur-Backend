package cl.deliciasdelsur.backend.service;

import cl.deliciasdelsur.backend.dto.CategoriaRequest;
import cl.deliciasdelsur.backend.exception.RecursoNoEncontradoException;
import cl.deliciasdelsur.backend.exception.ReglaNegocioException;
import cl.deliciasdelsur.backend.model.Categoria;
import cl.deliciasdelsur.backend.model.Producto;
import cl.deliciasdelsur.backend.repository.CategoriaRepository;
import cl.deliciasdelsur.backend.repository.ProductoRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

// Pruebas de las reglas de categorías: nombre único y borrado seguro
@ExtendWith(MockitoExtension.class)
class CategoriaServiceTest {

    @Mock
    private CategoriaRepository categoriaRepository;
    @Mock
    private ProductoRepository productoRepository;

    @InjectMocks
    private CategoriaService categoriaService;

    private CategoriaRequest solicitud(String nombre) {
        CategoriaRequest request = new CategoriaRequest();
        request.setNombre(nombre);
        request.setDescripcion("Descripción");
        return request;
    }

    @Test
    void crearGuardaLaCategoriaNueva() {
        when(categoriaRepository.existsByNombre("Dulces")).thenReturn(false);
        when(categoriaRepository.save(any(Categoria.class))).thenAnswer(inv -> inv.getArgument(0));

        Categoria creada = categoriaService.crear(solicitud("Dulces"));

        assertEquals("Dulces", creada.getNombre());
    }

    @Test
    void crearConNombreDuplicadoLanzaExcepcion() {
        when(categoriaRepository.existsByNombre("Dulces")).thenReturn(true);

        assertThrows(ReglaNegocioException.class, () -> categoriaService.crear(solicitud("Dulces")));
        verify(categoriaRepository, never()).save(any());
    }

    @Test
    void eliminarCategoriaConProductosLanzaExcepcion() {
        Categoria categoria = Categoria.builder().id(1L).nombre("Dulces").build();
        when(categoriaRepository.findById(1L)).thenReturn(Optional.of(categoria));
        when(productoRepository.findByCategoriaId(1L)).thenReturn(List.of(new Producto()));

        assertThrows(ReglaNegocioException.class, () -> categoriaService.eliminar(1L));
        verify(categoriaRepository, never()).delete(any());
    }

    @Test
    void obtenerCategoriaInexistenteLanzaExcepcion() {
        when(categoriaRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(RecursoNoEncontradoException.class, () -> categoriaService.obtenerPorId(99L));
    }
}