package cl.deliciasdelsur.backend.service;

import cl.deliciasdelsur.backend.dto.UsuarioRequest;
import cl.deliciasdelsur.backend.exception.RecursoNoEncontradoException;
import cl.deliciasdelsur.backend.exception.ReglaNegocioException;
import cl.deliciasdelsur.backend.model.Usuario;
import cl.deliciasdelsur.backend.repository.UsuarioRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

// Lógica de negocio de usuarios: correo único, contraseña encriptada y borrado seguro
@Service
@RequiredArgsConstructor
public class UsuarioService {

    private final UsuarioRepository usuarioRepository;
    private final PasswordEncoder passwordEncoder;

    public List<Usuario> listar() {
        return usuarioRepository.findAll();
    }

    public Usuario obtenerPorId(Long id) {
        return usuarioRepository.findById(id)
                .orElseThrow(() -> new RecursoNoEncontradoException("Usuario no encontrado con id " + id));
    }

    @Transactional
    public Usuario crear(UsuarioRequest request) {
        if (usuarioRepository.existsByCorreo(request.getCorreo())) {
            throw new ReglaNegocioException("Ya existe un usuario con ese correo");
        }
        if (request.getPassword() == null || request.getPassword().length() < 6) {
            throw new ReglaNegocioException("La contraseña debe tener al menos 6 caracteres");
        }
        Usuario usuario = Usuario.builder()
                .nombre(request.getNombre())
                .correo(request.getCorreo())
                .password(passwordEncoder.encode(request.getPassword()))
                .rol(request.getRol())
                .direccion(request.getDireccion())
                .telefono(request.getTelefono())
                .estado(request.getEstado() == null ? true : request.getEstado())
                .build();
        return usuarioRepository.save(usuario);
    }

    @Transactional
    public Usuario actualizar(Long id, UsuarioRequest request) {
        Usuario usuario = obtenerPorId(id);
        boolean cambioCorreo = !usuario.getCorreo().equals(request.getCorreo());
        if (cambioCorreo && usuarioRepository.existsByCorreo(request.getCorreo())) {
            throw new ReglaNegocioException("Ya existe un usuario con ese correo");
        }
        usuario.setNombre(request.getNombre());
        usuario.setCorreo(request.getCorreo());
        usuario.setRol(request.getRol());
        usuario.setDireccion(request.getDireccion());
        usuario.setTelefono(request.getTelefono());
        if (request.getEstado() != null) {
            usuario.setEstado(request.getEstado());
        }
        if (request.getPassword() != null && !request.getPassword().isBlank()) {
            if (request.getPassword().length() < 6) {
                throw new ReglaNegocioException("La contraseña debe tener al menos 6 caracteres");
            }
            usuario.setPassword(passwordEncoder.encode(request.getPassword()));
        }
        return usuarioRepository.save(usuario);
    }

    @Transactional
    public void eliminar(Long id) {
        Usuario usuario = obtenerPorId(id);
        try {
            usuarioRepository.delete(usuario);
            usuarioRepository.flush();
        } catch (DataIntegrityViolationException e) {
            throw new ReglaNegocioException("No se puede eliminar un usuario que tiene boletas registradas");
        }
    }
}