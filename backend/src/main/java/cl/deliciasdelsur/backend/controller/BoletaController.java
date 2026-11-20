package cl.deliciasdelsur.backend.controller;

import cl.deliciasdelsur.backend.dto.BoletaRequest;
import cl.deliciasdelsur.backend.model.Boleta;
import cl.deliciasdelsur.backend.service.BoletaService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

// Endpoints de boletas: el cliente compra y ve las suyas, administrador y vendedor ven todas
@RestController
@RequestMapping("/api/v1/boletas")
@RequiredArgsConstructor
@Tag(name = "Boletas", description = "Compras y órdenes del sistema")
public class BoletaController {

    private final BoletaService boletaService;

    @PostMapping
    @Operation(summary = "Crear una compra (el cliente sale del token)")
    public ResponseEntity<Boleta> crear(Authentication auth, @Valid @RequestBody BoletaRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(boletaService.crear(auth.getName(), request));
    }

    @GetMapping("/mis-boletas")
    @Operation(summary = "Listar solo las boletas del usuario del token")
    public List<Boleta> misBoletas(Authentication auth) {
        return boletaService.listarDelCliente(auth.getName());
    }

    @GetMapping
    @Operation(summary = "Listar todas las boletas (administrador y vendedor)")
    public List<Boleta> listar() {
        return boletaService.listarTodas();
    }

    @GetMapping("/{id}")
    @Operation(summary = "Ver el detalle de una boleta (administrador y vendedor)")
    public Boleta obtener(@PathVariable Long id) {
        return boletaService.obtenerPorId(id);
    }
}