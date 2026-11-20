package cl.deliciasdelsur.backend.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

// Datos que se reciben para crear o editar una categoría
@Data
public class CategoriaRequest {

    @NotBlank(message = "El nombre de la categoría es obligatorio")
    private String nombre;

    private String descripcion;
}