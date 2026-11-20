package cl.deliciasdelsur.backend.controller;

import cl.deliciasdelsur.backend.dto.ProductoRequest;
import cl.deliciasdelsur.backend.model.Producto;
import cl.deliciasdelsur.backend.service.ProductoService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

// Endpoints de productos: lectura pública, escritura solo para ADMINISTRADOR
@RestController
@RequestMapping("/api/v1/productos")
@RequiredArgsConstructor
@Tag(name = "Productos", description = "Gestión del catálogo de productos")
public class ProductoController {

    private final ProductoService productoService;

    @GetMapping
    @Operation(summary = "Listar productos, con filtro opcional por categoría o nombre")
    public List<Producto> listar(@RequestParam(required = false) Long categoriaId,
                                 @RequestParam(required = false) String nombre) {
        return productoService.listar(categoriaId, nombre);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Obtener el detalle de un producto")
    public Producto obtener(@PathVariable Long id) {
        return productoService.obtenerPorId(id);
    }

    @PostMapping
    @Operation(summary = "Crear un producto")
    public ResponseEntity<Producto> crear(@Valid @RequestBody ProductoRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(productoService.crear(request));
    }

    @PutMapping("/{id}")
    @Operation(summary = "Editar un producto")
    public Producto actualizar(@PathVariable Long id, @Valid @RequestBody ProductoRequest request) {
        return productoService.actualizar(id, request);
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Eliminar un producto sin ventas")
    public ResponseEntity<Void> eliminar(@PathVariable Long id) {
        productoService.eliminar(id);
        return ResponseEntity.noContent().build();
    }
}