package co.edu.unbosque.reventacero.service;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Optional;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import co.edu.unbosque.reventacero.dto.LoginDTO;
import co.edu.unbosque.reventacero.dto.RegistroDTO;
import co.edu.unbosque.reventacero.entity.NombreCompleto;
import co.edu.unbosque.reventacero.entity.Usuario;
import co.edu.unbosque.reventacero.enums.TipoUsuario;
import co.edu.unbosque.reventacero.exception.CorreoYaExisteException;
import co.edu.unbosque.reventacero.exception.CredencialesInvalidasException;
import co.edu.unbosque.reventacero.exception.DocumentoYaExisteException;
import co.edu.unbosque.reventacero.exception.UsuarioYaExisteException;
import co.edu.unbosque.reventacero.repository.UsuarioRepository;

@Service
public class AutenticacionService {

	private static final String ESTADO_CUENTA_ACTIVA = "ACTIVA";

	private static final int LONGITUD_MINIMA_CONTRASENIA = 8;

	private final UsuarioRepository usuarioRepo;

	private final PasswordEncoder passwordEncoder;

	public AutenticacionService(UsuarioRepository usuarioRepo, PasswordEncoder passwordEncoder) {
		this.usuarioRepo = usuarioRepo;
		this.passwordEncoder = passwordEncoder;
	}

	/**
	 * Registra un usuario nuevo con rol USUARIO.
	 * Los organizadores y administradores se crean desde el panel de administración.
	 */
	public Usuario registrar(RegistroDTO dto) {

		validarRegistro(dto);

		if (usuarioRepo.existsByDocumento(dto.getDocumento())) {
			throw new DocumentoYaExisteException();
		}

		if (usuarioRepo.findByNombreUsuario(dto.getNombreUsuario()).isPresent()) {
			throw new UsuarioYaExisteException();
		}

		if (usuarioRepo.findByCorreo(dto.getCorreo()).isPresent()) {
			throw new CorreoYaExisteException();
		}

		Usuario usuario = new Usuario();
		usuario.setDocumento(dto.getDocumento());
		usuario.setNombreUsuario(dto.getNombreUsuario());
		usuario.setContrasenia(passwordEncoder.encode(dto.getContrasenia()));
		usuario.setFechaRegistro(LocalDate.now());
		usuario.setEstadoCuenta(ESTADO_CUENTA_ACTIVA);
		usuario.setNombreCompleto(new NombreCompleto(dto.getNombre(), dto.getApellido()));
		usuario.setCorreo(dto.getCorreo());
		usuario.setFechaNacimiento(dto.getFechaNacimiento());
		usuario.setTipoUsuario(TipoUsuario.USUARIO);
		usuario.setTelefono(dto.getTelefono() != null ? dto.getTelefono() : new ArrayList<>());

		usuarioRepo.save(usuario);

		return usuario;
	}

	public Usuario login(LoginDTO dto) {

		if (dto.getNombreUsuario() == null || dto.getContrasenia() == null) {
			throw new CredencialesInvalidasException();
		}

		Optional<Usuario> usuario = usuarioRepo.findByNombreUsuario(dto.getNombreUsuario());

		if (usuario.isEmpty()
				|| !passwordEncoder.matches(dto.getContrasenia(), usuario.get().getContrasenia())) {
			throw new CredencialesInvalidasException();
		}

		return usuario.get();
	}

	private void validarRegistro(RegistroDTO dto) {

		if (dto.getDocumento() <= 0) {
			throw new IllegalArgumentException("El documento es obligatorio");
		}

		if (estaVacio(dto.getNombreUsuario())) {
			throw new IllegalArgumentException("El nombre de usuario es obligatorio");
		}

		if (estaVacio(dto.getCorreo()) || !dto.getCorreo().matches("^[^@\\s]+@[^@\\s]+\\.[^@\\s]+$")) {
			throw new IllegalArgumentException("El correo no es válido");
		}

		if (dto.getContrasenia() == null || dto.getContrasenia().length() < LONGITUD_MINIMA_CONTRASENIA) {
			throw new IllegalArgumentException(
					"La contraseña debe tener al menos " + LONGITUD_MINIMA_CONTRASENIA + " caracteres");
		}

		if (estaVacio(dto.getNombre()) || estaVacio(dto.getApellido())) {
			throw new IllegalArgumentException("El nombre y el apellido son obligatorios");
		}
	}

	private static boolean estaVacio(String valor) {
		return valor == null || valor.isBlank();
	}

}
