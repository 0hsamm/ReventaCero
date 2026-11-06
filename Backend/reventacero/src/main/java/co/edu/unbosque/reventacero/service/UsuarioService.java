package co.edu.unbosque.reventacero.service;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import org.modelmapper.ModelMapper;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import co.edu.unbosque.reventacero.dto.UsuarioDTO;
import co.edu.unbosque.reventacero.entity.NombreCompleto;
import co.edu.unbosque.reventacero.entity.Usuario;
import co.edu.unbosque.reventacero.enums.TipoUsuario;
import co.edu.unbosque.reventacero.exception.RecursoNoEncontradoException;
import co.edu.unbosque.reventacero.exception.ReglaNegocioException;
import co.edu.unbosque.reventacero.repository.UsuarioRepository;
import co.edu.unbosque.reventacero.util.Validaciones;

@Service
public class UsuarioService {

	public static final String ESTADO_ACTIVA = "ACTIVA";

	private static final Set<String> ESTADOS_VALIDOS = Set.of("ACTIVA", "SUSPENDIDA", "BLOQUEADA");

	private static final int LONGITUD_MINIMA_CONTRASENIA = 8;

	private static final String PATRON_CORREO = "^[^@\\s]+@[^@\\s]+\\.[^@\\s]+$";

	private final UsuarioRepository usuarioRepo;

	private final PasswordEncoder passwordEncoder;

	private final ModelMapper modelMapper;

	public UsuarioService(UsuarioRepository usuarioRepo, PasswordEncoder passwordEncoder, ModelMapper modelMapper) {
		this.usuarioRepo = usuarioRepo;
		this.passwordEncoder = passwordEncoder;
		this.modelMapper = modelMapper;
	}

	public UsuarioDTO buscarPorDocumento(long documento) {
		return aDTO(buscarEntidad(documento));
	}

	public List<UsuarioDTO> listar() {
		return usuarioRepo.findAll().stream().map(this::aDTO).toList();
	}

	@Transactional
	public UsuarioDTO actualizar(long documento, UsuarioDTO dto) {

		Usuario usuario = buscarEntidad(documento);
		validarDatosPersonales(dto, documento);

		usuario.setNombreUsuario(dto.getNombreUsuario().trim());
		usuario.setNombreCompleto(new NombreCompleto(dto.getNombre().trim(), dto.getApellido().trim()));
		usuario.setCorreo(dto.getCorreo().trim());
		usuario.setFechaNacimiento(dto.getFechaNacimiento());
		usuario.setTelefono(limpiarTelefonos(dto.getTelefono()));

		try {
			usuarioRepo.update(usuario);
		} catch (DataIntegrityViolationException e) {
			throw new ReglaNegocioException("El nombre de usuario o el correo ya están registrados");
		}

		return aDTO(usuario);
	}

	@Transactional
	public void cambiarContrasenia(long documento, String contraseniaActual, String contraseniaNueva) {

		Usuario usuario = buscarEntidad(documento);

		Validaciones.requerirTexto(contraseniaActual, "contraseniaActual");
		validarContrasenia(contraseniaNueva);

		if (!passwordEncoder.matches(contraseniaActual, usuario.getContrasenia())) {
			throw new ReglaNegocioException("La contraseña actual no es correcta");
		}

		if (passwordEncoder.matches(contraseniaNueva, usuario.getContrasenia())) {
			throw new ReglaNegocioException("La contraseña nueva debe ser diferente a la actual");
		}

		usuarioRepo.actualizarContrasenia(documento, passwordEncoder.encode(contraseniaNueva));
	}

	@Transactional
	public UsuarioDTO cambiarEstado(long documento, String estado) {

		Usuario usuario = buscarEntidad(documento);

		Validaciones.requerirTexto(estado, "estado");
		String estadoNormalizado = estado.trim().toUpperCase();

		if (!ESTADOS_VALIDOS.contains(estadoNormalizado)) {
			throw new IllegalArgumentException("El estado debe ser uno de: " + String.join(", ", ESTADOS_VALIDOS));
		}

		usuarioRepo.actualizarEstado(documento, estadoNormalizado);
		usuario.setEstadoCuenta(estadoNormalizado);

		return aDTO(usuario);
	}

	@Transactional
	public void eliminar(long documento) {

		buscarEntidad(documento);

		try {
			usuarioRepo.deleteById(documento);
		} catch (DataIntegrityViolationException e) {
			throw new ReglaNegocioException(
					"No se puede eliminar el usuario porque tiene registros asociados (boletas, dispositivos, "
							+ "festivales o un rol de organizador o administrador)");
		}
	}

	public void validarNuevo(UsuarioDTO dto) {

		Validaciones.requerirPositivo(dto.getDocumento(), "documento");
		validarDatosPersonales(dto, 0);
		validarContrasenia(dto.getContrasenia());

		if (usuarioRepo.existsByDocumento(dto.getDocumento())) {
			throw new ReglaNegocioException("Ya existe un usuario con el documento " + dto.getDocumento());
		}
	}

	public void prepararNuevo(Usuario usuario, UsuarioDTO dto, TipoUsuario tipo) {
		usuario.setDocumento(dto.getDocumento());
		usuario.setNombreUsuario(dto.getNombreUsuario().trim());
		usuario.setContrasenia(passwordEncoder.encode(dto.getContrasenia()));
		usuario.setFechaRegistro(LocalDate.now());
		usuario.setEstadoCuenta(ESTADO_ACTIVA);
		usuario.setNombreCompleto(new NombreCompleto(dto.getNombre().trim(), dto.getApellido().trim()));
		usuario.setCorreo(dto.getCorreo().trim());
		usuario.setFechaNacimiento(dto.getFechaNacimiento());
		usuario.setTipoUsuario(tipo);
		usuario.setTelefono(limpiarTelefonos(dto.getTelefono()));
	}

	public void completarDTO(Usuario usuario, UsuarioDTO dto) {
		dto.setContrasenia(null);
		dto.setTipoUsuario(usuario.getTipoUsuario() != null ? usuario.getTipoUsuario().name() : null);
		dto.setNombre(usuario.getNombreCompleto() != null ? usuario.getNombreCompleto().getNombre() : null);
		dto.setApellido(usuario.getNombreCompleto() != null ? usuario.getNombreCompleto().getApellido() : null);
		dto.setAutorizadoPorDocumento(
				usuario.getAutorizadoPor() != null ? usuario.getAutorizadoPor().getDocumento() : null);
		dto.setTelefono(usuario.getTelefono() != null ? new ArrayList<>(usuario.getTelefono()) : new ArrayList<>());
	}

	private UsuarioDTO aDTO(Usuario usuario) {
		UsuarioDTO dto = modelMapper.map(usuario, UsuarioDTO.class);
		completarDTO(usuario, dto);
		return dto;
	}

	private Usuario buscarEntidad(long documento) {
		return usuarioRepo.findByDocumento(documento)
				.orElseThrow(() -> new RecursoNoEncontradoException("El usuario " + documento + " no existe"));
	}

	private void validarDatosPersonales(UsuarioDTO dto, long documentoPropio) {

		Validaciones.requerirTexto(dto.getNombreUsuario(), "nombreUsuario");
		Validaciones.requerirTexto(dto.getNombre(), "nombre");
		Validaciones.requerirTexto(dto.getApellido(), "apellido");
		Validaciones.requerirTexto(dto.getCorreo(), "correo");

		if (!dto.getCorreo().trim().matches(PATRON_CORREO)) {
			throw new IllegalArgumentException("El correo no es válido");
		}

		if (dto.getFechaNacimiento() != null && !dto.getFechaNacimiento().isBefore(LocalDate.now())) {
			throw new IllegalArgumentException("La fecha de nacimiento debe ser anterior a hoy");
		}

		Optional<Usuario> conNombre = usuarioRepo.findByNombreUsuario(dto.getNombreUsuario().trim());
		if (conNombre.isPresent() && conNombre.get().getDocumento() != documentoPropio) {
			throw new ReglaNegocioException("El nombre de usuario ya está en uso");
		}

		Optional<Usuario> conCorreo = usuarioRepo.findByCorreo(dto.getCorreo().trim());
		if (conCorreo.isPresent() && conCorreo.get().getDocumento() != documentoPropio) {
			throw new ReglaNegocioException("El correo ya está registrado");
		}
	}

	private void validarContrasenia(String contrasenia) {
		if (contrasenia == null || contrasenia.length() < LONGITUD_MINIMA_CONTRASENIA) {
			throw new IllegalArgumentException(
					"La contraseña debe tener al menos " + LONGITUD_MINIMA_CONTRASENIA + " caracteres");
		}
	}

	private List<String> limpiarTelefonos(List<String> telefonos) {
		Set<String> limpios = new LinkedHashSet<>();
		if (telefonos != null) {
			for (String telefono : telefonos) {
				if (!Validaciones.estaVacio(telefono)) {
					String valor = telefono.trim();
					if (valor.length() > 40) {
						throw new IllegalArgumentException("Un teléfono no puede tener más de 40 caracteres");
					}
					limpios.add(valor);
				}
			}
		}
		return new ArrayList<>(limpios);
	}

	public void validarCuentaActiva(long documento) {

		Usuario usuario = buscarEntidad(documento);

		if (!ESTADO_ACTIVA.equalsIgnoreCase(usuario.getEstadoCuenta())) {
			throw new ReglaNegocioException("La cuenta del usuario " + documento + " no está activa");
		}
	}
}