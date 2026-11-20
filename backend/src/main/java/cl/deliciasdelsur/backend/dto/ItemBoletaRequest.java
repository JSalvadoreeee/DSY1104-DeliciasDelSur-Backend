package cl.deliciasdelsur.backend.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

// Un producto y su cantidad dentro de la compra
@Data
public class ItemBoletaRequest {

    @NotNull(message = "El producto es obligatorio")
    private Long productoId;

    @NotNull(message = "La cantidad es obligatoria")
    @Min(value = 1, message = "La cantidad debe ser al menos 1")
    private Integer cantidad;
}