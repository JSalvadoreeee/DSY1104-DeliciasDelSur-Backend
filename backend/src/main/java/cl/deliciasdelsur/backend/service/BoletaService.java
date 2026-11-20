package cl.deliciasdelsur.backend.service;

import cl.deliciasdelsur.backend.dto.BoletaRequest;
import cl.deliciasdelsur.backend.dto.ItemBoletaRequest;
import cl.deliciasdelsur.backend.exception.RecursoNoEncontradoException;
import cl.deliciasdelsur.backend.exception.ReglaNegocioException;
import cl.deliciasdelsur.backend.model.Boleta;
import cl.deliciasdelsur.backend.model.DetalleBoleta;
import cl.deliciasdelsur.backend.model.Producto;
import cl.deliciasdelsur.backend.model.Usuario;
import cl.deliciasdelsur.backend.repository.BoletaRepository;
import cl.deliciasdelsur.backend.repository.ProductoRepository;
import cl.deliciasdelsur.backend.repository.UsuarioRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

// Lógica de boletas: valida stock, calcula el total en el servidor y descuenta stock en una sola transacción
@Service
@RequiredArgsConstructor
public class BoletaService {

    private final BoletaRepository boletaRepository;
    private final ProductoRepository productoRepository;
    private final UsuarioRepository usuarioRepository;

    @Transactional
    public Boleta crear(String correoCliente, BoletaRequest request) {
        Usuario cliente = usuarioRepository.findByCorreo(correoCliente)
                .orElseThrow(() -> new RecursoNoEncontradoException("Usuario no encontrado"));

        Map<Long, Integer> cantidades = new LinkedHashMap<>();
        for (ItemBoletaRequest item : request.getItems()) {
            cantidades.merge(item.getProductoId(), item.getCantidad(), Integer::sum);
        }

        Boleta boleta = Boleta.builder().usuario(cliente).build();
        int total = 0;

        for (Map.Entry<Long, Integer> entrada : cantidades.entrySet()) {
            Producto producto = productoRepository.buscarParaActualizar(entrada.getKey())
                    .orElseThrow(() -> new RecursoNoEncontradoException(
                            "Producto no encontrado con id " + entrada.getKey()));

            int cantidad = entrada.getValue();
            if (producto.getStock() < cantidad) {
                throw new ReglaNegocioException("Stock insuficiente para " + producto.getNombre()
                        + " (disponible: " + producto.getStock() + ")");
            }

            int precioUnitario = producto.getPrecio() * (100 - producto.getOferta()) / 100;
            int subtotal = precioUnitario * cantidad;

            producto.setStock(producto.getStock() - cantidad);
            productoRepository.save(producto);

            boleta.getDetalles().add(DetalleBoleta.builder()
                    .boleta(boleta)
                    .producto(producto)
                    .cantidad(cantidad)
                    .precioUnitario(precioUnitario)
                    .subtotal(subtotal)
                    .build());
            total += subtotal;
        }

        boleta.setTotal(total);
        return boletaRepository.save(boleta);
    }

    public List<Boleta> listarTodas() {
        return boletaRepository.findAllByOrderByFechaDesc();
    }

    public Boleta obtenerPorId(Long id) {
        return boletaRepository.findById(id)
                .orElseThrow(() -> new RecursoNoEncontradoException("Boleta no encontrada con id " + id));
    }

    public List<Boleta> listarDelCliente(String correo) {
        return boletaRepository.findByUsuarioCorreoOrderByFechaDesc(correo);
    }
}