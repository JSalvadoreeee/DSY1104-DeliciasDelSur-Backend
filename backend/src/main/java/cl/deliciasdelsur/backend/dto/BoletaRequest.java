package cl.deliciasdelsur.backend.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import lombok.Data;

import java.util.List;

// Compra que envía el cliente: solo productos y cantidades, el total lo calcula el servidor
@Data
public class BoletaRequest {

    @NotEmpty(message = "La compra debe tener al menos un producto")
    @Valid
    private List<ItemBoletaRequest> items;
}