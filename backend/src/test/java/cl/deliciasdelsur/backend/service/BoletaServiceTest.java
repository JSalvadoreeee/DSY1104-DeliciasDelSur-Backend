package cl.deliciasdelsur.backend.service;

import cl.deliciasdelsur.backend.dto.BoletaRequest;
import cl.deliciasdelsur.backend.dto.ItemBoletaRequest;
import cl.deliciasdelsur.backend.exception.RecursoNoEncontradoException;
import cl.deliciasdelsur.backend.exception.ReglaNegocioException;
import cl.deliciasdelsur.backend.model.Boleta;
import cl.deliciasdelsur.backend.model.Producto;
import cl.deliciasdelsur.backend.model.Rol;
import cl.deliciasdelsur.backend.model.Usuario;
import cl.deliciasdelsur.backend.repository.BoletaRepository;
import cl.deliciasdelsur.backend.repository.ProductoRepository;
import cl.deliciasdelsur.backend.repository.UsuarioRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

// Pruebas de la lógica de negocio de boletas: total, descuento de stock y errores
@ExtendWith(MockitoExtension.class)
class BoletaServiceTest {

    @Mock
    private BoletaRepository boletaRepository;
    @Mock
    private ProductoRepository productoRepository;
    @Mock
    private UsuarioRepository usuarioRepository;

    @InjectMocks
    private BoletaService boletaService;

    private Usuario cliente;
    private Producto producto;

    @BeforeEach
    void preparar() {
        cliente = Usuario.builder().id(3L).nombre("Cliente").correo("cliente@test.cl").rol(Rol.CLIENTE).build();
        producto = Producto.builder().id(1L).nombre("Kuchen").precio(10000).stock(5).oferta(10).build();
    }

    private ItemBoletaRequest item(Long productoId, int cantidad) {
        ItemBoletaRequest item = new ItemBoletaRequest();
        item.setProductoId(productoId);
        item.setCantidad(cantidad);
        return item;
    }

    private BoletaRequest solicitud(ItemBoletaRequest... items) {
        BoletaRequest request = new BoletaRequest();
        request.setItems(List.of(items));
        return request;
    }

    @Test
    void crearCalculaElTotalConOfertaYDescuentaStock() {
        when(usuarioRepository.findByCorreo("cliente@test.cl")).thenReturn(Optional.of(cliente));
        when(productoRepository.buscarParaActualizar(1L)).thenReturn(Optional.of(producto));
        when(boletaRepository.save(any(Boleta.class))).thenAnswer(inv -> inv.getArgument(0));

        Boleta boleta = boletaService.crear("cliente@test.cl", solicitud(item(1L, 2)));

        assertEquals(18000, boleta.getTotal());
        assertEquals(9000, boleta.getDetalles().get(0).getPrecioUnitario());
        assertEquals(1, boleta.getDetalles().size());
        assertEquals(3, producto.getStock());
        verify(productoRepository).save(producto);
    }

    @Test
    void crearSumaLasCantidadesDeUnProductoRepetido() {
        when(usuarioRepository.findByCorreo("cliente@test.cl")).thenReturn(Optional.of(cliente));
        when(productoRepository.buscarParaActualizar(1L)).thenReturn(Optional.of(producto));
        when(boletaRepository.save(any(Boleta.class))).thenAnswer(inv -> inv.getArgument(0));

        Boleta boleta = boletaService.crear("cliente@test.cl", solicitud(item(1L, 2), item(1L, 3)));

        assertEquals(1, boleta.getDetalles().size());
        assertEquals(45000, boleta.getTotal());
        assertEquals(0, producto.getStock());
    }

    @Test
    void crearConStockInsuficienteLanzaExcepcionYNoGuarda() {
        when(usuarioRepository.findByCorreo("cliente@test.cl")).thenReturn(Optional.of(cliente));
        when(productoRepository.buscarParaActualizar(1L)).thenReturn(Optional.of(producto));

        ReglaNegocioException error = assertThrows(ReglaNegocioException.class,
                () -> boletaService.crear("cliente@test.cl", solicitud(item(1L, 6))));

        assertTrue(error.getMessage().contains("Stock insuficiente"));
        assertEquals(5, producto.getStock());
        verify(boletaRepository, never()).save(any());
    }

    @Test
    void crearConProductoInexistenteLanzaExcepcion() {
        when(usuarioRepository.findByCorreo("cliente@test.cl")).thenReturn(Optional.of(cliente));
        when(productoRepository.buscarParaActualizar(99L)).thenReturn(Optional.empty());

        assertThrows(RecursoNoEncontradoException.class,
                () -> boletaService.crear("cliente@test.cl", solicitud(item(99L, 1))));
        verify(boletaRepository, never()).save(any());
    }

    @Test
    void crearConUsuarioInexistenteLanzaExcepcion() {
        when(usuarioRepository.findByCorreo("nadie@test.cl")).thenReturn(Optional.empty());

        assertThrows(RecursoNoEncontradoException.class,
                () -> boletaService.crear("nadie@test.cl", solicitud(item(1L, 1))));
    }
}