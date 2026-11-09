package co.edu.unbosque.reventacero.service;

import java.time.Duration;
import java.time.Instant;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import co.edu.unbosque.reventacero.dto.LoginDTO;
import co.edu.unbosque.reventacero.dto.RegistroDTO;
import co.edu.unbosque.reventacero.entity.NombreCompleto;
import co.edu.unbosque.reventacero.entity.Usuario;
import co.edu.unbosque.reventacero.enums.TipoUsuario;
import co.edu.unbosque.reventacero.exception.CorreoYaExisteException;
import co.edu.unbosque.reventacero.exception.CredencialesInvalidasException;
import co.edu.unbosque.reventacero.exception.CuentaNoVerificadaException;
import co.edu.unbosque.reventacero.exception.DocumentoYaExisteException;
import co.edu.unbosque.reventacero.exception.ReglaNegocioException;
import co.edu.unbosque.reventacero.exception.UsuarioYaExisteException;
import co.edu.unbosque.reventacero.repository.UsuarioRepository;
import co.edu.unbosque.reventacero.security.JwtUtil;

@Service
public class AutenticacionService {

	public static final String ESTADO_CUENTA_PENDIENTE = "PENDIENTE";

	private static final String ESTADO_CUENTA_ACTIVA = UsuarioService.ESTADO_ACTIVA;

	private static final int LONGITUD_MINIMA_CONTRASENIA = 8;

	private static final Duration ESPERA_REENVIO = Duration.ofSeconds(60);

	public static final String ENLACE_INVALIDO =
			"El enlace de verificación no es válido o ya venció. Pide uno nuevo desde la página de verificación.";

	public enum ResultadoVerificacion {
		VERIFICADA, YA_VERIFICADA
	}

	private final UsuarioRepository usuarioRepo;

	private final PasswordEncoder passwordEncoder;

	private final JwtUtil jwtUtil;

	private final CorreoService correoService;

	private final long horasValidezEnlace;

	private final Map<Long, Instant> ultimosEnvios = new ConcurrentHashMap<>();

	public AutenticacionService(UsuarioRepository usuarioRepo, PasswordEncoder passwordEncoder, JwtUtil jwtUtil,
			CorreoService correoService, @Value("${app.verificacion.horas:24}") long horasValidezEnlace) {
		this.usuarioRepo = usuarioRepo;
		this.passwordEncoder = passwordEncoder;
		this.jwtUtil = jwtUtil;
		this.correoService = correoService;
		this.horasValidezEnlace = horasValidezEnlace;
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
		usuario.setEstadoCuenta(ESTADO_CUENTA_PENDIENTE);
		usuario.setNombreCompleto(new NombreCompleto(dto.getNombre(), dto.getApellido()));
		usuario.setCorreo(dto.getCorreo());
		usuario.setFechaNacimiento(dto.getFechaNacimiento());
		usuario.setTipoUsuario(TipoUsuario.USUARIO);
		usuario.setTelefono(dto.getTelefono() != null ? dto.getTelefono() : new ArrayList<>());

		usuarioRepo.save(usuario);

		return usuario;
	}

	public boolean enviarCorreoVerificacion(Usuario usuario) {
		String token = jwtUtil.generarTokenVerificacion(usuario, horasValidezEnlace);
		boolean enviado = correoService.enviarVerificacion(usuario, token, horasValidezEnlace);
		if (enviado) {
			ultimosEnvios.put(usuario.getDocumento(), Instant.now());
		}
		return enviado;
	}

	public ResultadoVerificacion verificarCorreo(String token) {

		if (estaVacio(token)) {
			throw new IllegalArgumentException(ENLACE_INVALIDO);
		}

		JwtUtil.VerificacionCorreo datos = jwtUtil.leerTokenVerificacion(token.trim())
				.orElseThrow(() -> new IllegalArgumentException(ENLACE_INVALIDO));

		Usuario usuario = usuarioRepo.findByDocumento(datos.documento())
				.filter(u -> u.getCorreo() != null && u.getCorreo().equalsIgnoreCase(datos.correo()))
				.orElseThrow(() -> new IllegalArgumentException(ENLACE_INVALIDO));

		if (ESTADO_CUENTA_ACTIVA.equalsIgnoreCase(usuario.getEstadoCuenta())) {
			return ResultadoVerificacion.YA_VERIFICADA;
		}

		if (!ESTADO_CUENTA_PENDIENTE.equalsIgnoreCase(usuario.getEstadoCuenta())) {
			throw new ReglaNegocioException("Esta cuenta no se puede activar. Comunícate con soporte.");
		}

		usuarioRepo.actualizarEstado(usuario.getDocumento(), ESTADO_CUENTA_ACTIVA);
		ultimosEnvios.remove(usuario.getDocumento());

		return ResultadoVerificacion.VERIFICADA;
	}

	public void reenviarVerificacion(String correoONombreUsuario) {

		if (estaVacio(correoONombreUsuario)) {
			throw new IllegalArgumentException("Escribe tu correo o tu nombre de usuario");
		}

		String valor = correoONombreUsuario.trim();
		Optional<Usuario> usuario = valor.contains("@")
				? usuarioRepo.findByCorreo(valor)
				: usuarioRepo.findByNombreUsuario(valor);

		usuario.filter(u -> ESTADO_CUENTA_PENDIENTE.equalsIgnoreCase(u.getEstadoCuenta()))
				.filter(this::puedeReenviar)
				.ifPresent(this::enviarCorreoVerificacion);
	}

	private boolean puedeReenviar(Usuario usuario) {
		Instant ultimo = ultimosEnvios.get(usuario.getDocumento());
		return ultimo == null || ultimo.plus(ESPERA_REENVIO).isBefore(Instant.now());
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

		if (ESTADO_CUENTA_PENDIENTE.equalsIgnoreCase(usuario.get().getEstadoCuenta())) {
			throw new CuentaNoVerificadaException();
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
