package cl.deliciasdelsur.backend.controller;

import cl.deliciasdelsur.backend.dto.AuthResponse;
import cl.deliciasdelsur.backend.dto.LoginRequest;
import cl.deliciasdelsur.backend.dto.RegistroRequest;
import cl.deliciasdelsur.backend.model.Rol;
import cl.deliciasdelsur.backend.model.Usuario;
import cl.deliciasdelsur.backend.repository.UsuarioRepository;
import cl.deliciasdelsur.backend.security.JwtService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
@Tag(name = "Autenticación", description = "Endpoints públicos para inicio de sesión y registro de clientes")
public class AuthController {

    private final AuthenticationManager authenticationManager;
    private final UsuarioRepository usuarioRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;

    @PostMapping("/login")
    @Operation(summary = "Iniciar sesión y obtener token JWT")
    public ResponseEntity<?> login(@Valid @RequestBody LoginRequest request) {
        try {
            authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(request.getCorreo(), request.getPassword())
            );
        } catch (BadCredentialsException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("mensaje", "Correo o contraseña incorrectos"));
        }

        Usuario usuario = usuarioRepository.findByCorreo(request.getCorreo())
                .orElseThrow();

        if (!usuario.getEstado()) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(Map.of("mensaje", "La cuenta se encuentra desactivada"));
        }

        User userDetails = new User(
                usuario.getCorreo(),
                usuario.getPassword(),
                List.of(new SimpleGrantedAuthority("ROLE_" + usuario.getRol().name()))
        );

        String token = jwtService.generateToken(
                userDetails,
                usuario.getRol().name(),
                usuario.getId(),
                usuario.getNombre()
        );

        return ResponseEntity.ok(AuthResponse.builder()
                .token(token)
                .id(usuario.getId())
                .nombre(usuario.getNombre())
                .correo(usuario.getCorreo())
                .rol(usuario.getRol().name())
                .build());
    }

    @PostMapping("/registro")
    @Operation(summary = "Registrar nuevo usuario con rol CLIENTE")
    public ResponseEntity<?> registro(@Valid @RequestBody RegistroRequest request) {
        if (usuarioRepository.existsByCorreo(request.getCorreo())) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(Map.of("mensaje", "El correo ya se encuentra registrado"));
        }

        Usuario nuevoUsuario = Usuario.builder()
                .nombre(request.getNombre())
                .correo(request.getCorreo())
                .password(passwordEncoder.encode(request.getPassword()))
                .rol(Rol.CLIENTE)
                .direccion(request.getDireccion())
                .telefono(request.getTelefono())
                .estado(true)
                .build();

        Usuario guardado = usuarioRepository.save(nuevoUsuario);

        User userDetails = new User(
                guardado.getCorreo(),
                guardado.getPassword(),
                List.of(new SimpleGrantedAuthority("ROLE_" + guardado.getRol().name()))
        );

        String token = jwtService.generateToken(
                userDetails,
                guardado.getRol().name(),
                guardado.getId(),
                guardado.getNombre()
        );

        return ResponseEntity.status(HttpStatus.CREATED).body(AuthResponse.builder()
                .token(token)
                .id(guardado.getId())
                .nombre(guardado.getNombre())
                .correo(guardado.getCorreo())
                .rol(guardado.getRol().name())
                .build());
    }
}