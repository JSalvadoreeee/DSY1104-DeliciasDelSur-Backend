package cl.deliciasdelsur.backend.service;

import cl.deliciasdelsur.backend.dto.UsuarioRequest;
import cl.deliciasdelsur.backend.exception.ReglaNegocioException;
import cl.deliciasdelsur.backend.model.Rol;
import cl.deliciasdelsur.backend.model.Usuario;
import cl.deliciasdelsur.backend.repository.UsuarioRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

// Pruebas de las reglas de usuarios: correo único y contraseña encriptada
@ExtendWith(MockitoExtension.class)
class UsuarioServiceTest {

    @Mock
    private UsuarioRepository usuarioRepository;
    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private UsuarioService usuarioService;

    private UsuarioRequest solicitud(String correo, String password) {
        UsuarioRequest request = new UsuarioRequest();
        request.setNombre("Persona Prueba");
        request.setCorreo(correo);
        request.setPassword(password);
        request.setRol(Rol.VENDEDOR);
        return request;
    }

    @Test
    void crearEncriptaLaContrasenaYDejaElUsuarioActivo() {
        when(usuarioRepository.existsByCorreo("nuevo@test.cl")).thenReturn(false);
        when(passwordEncoder.encode("secreto1")).thenReturn("HASH");
        when(usuarioRepository.save(any(Usuario.class))).thenAnswer(inv -> inv.getArgument(0));

        Usuario creado = usuarioService.crear(solicitud("nuevo@test.cl", "secreto1"));

        assertEquals("HASH", creado.getPassword());
        assertEquals(Rol.VENDEDOR, creado.getRol());
        assertTrue(creado.getEstado());
    }

    @Test
    void crearConCorreoDuplicadoLanzaExcepcion() {
        when(usuarioRepository.existsByCorreo("repetido@test.cl")).thenReturn(true);

        assertThrows(ReglaNegocioException.class,
                () -> usuarioService.crear(solicitud("repetido@test.cl", "secreto1")));
        verify(usuarioRepository, never()).save(any());
    }

    @Test
    void crearConContrasenaCortaLanzaExcepcion() {
        when(usuarioRepository.existsByCorreo("nuevo@test.cl")).thenReturn(false);

        assertThrows(ReglaNegocioException.class,
                () -> usuarioService.crear(solicitud("nuevo@test.cl", "123")));
        verify(usuarioRepository, never()).save(any());
    }
}