package cl.deliciasdelsur.backend.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

// Datos que se reciben para crear o editar un producto
@Data
public class ProductoRequest {

    @NotBlank(message = "El nombre del producto es obligatorio")
    private String nombre;

    private String descripcion;

    @NotNull(message = "El precio es obligatorio")
    @Min(value = 0, message = "El precio no puede ser negativo")
    private Integer precio;

    @NotNull(message = "El stock es obligatorio")
    @Min(value = 0, message = "El stock no puede ser negativo")
    private Integer stock;

    private String imagenUrl;

    @Min(value = 0, message = "La oferta no puede ser menor a 0")
    @Max(value = 100, message = "La oferta no puede ser mayor a 100")
    private Integer oferta;

    @NotNull(message = "La categoría es obligatoria")
    private Long categoriaId;
}