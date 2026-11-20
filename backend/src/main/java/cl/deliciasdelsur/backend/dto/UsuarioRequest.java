package cl.deliciasdelsur.backend.dto;

import cl.deliciasdelsur.backend.model.Rol;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

// Datos que recibe el administrador para crear o editar un usuario
@Data
public class UsuarioRequest {

    @NotBlank(message = "El nombre es obligatorio")
    private String nombre;

    @NotBlank(message = "El correo es obligatorio")
    @Email(message = "Formato de correo inválido")
    private String correo;

    private String password;

    @NotNull(message = "El rol es obligatorio")
    private Rol rol;

    private String direccion;

    private String telefono;

    private Boolean estado;
}