package co.edu.unbosque.reventacero.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.util.ReflectionTestUtils;

import co.edu.unbosque.reventacero.dto.LoginDTO;
import co.edu.unbosque.reventacero.dto.RegistroDTO;
import co.edu.unbosque.reventacero.entity.Usuario;
import co.edu.unbosque.reventacero.enums.TipoUsuario;
import co.edu.unbosque.reventacero.exception.CorreoYaExisteException;
import co.edu.unbosque.reventacero.exception.CredencialesInvalidasException;
import co.edu.unbosque.reventacero.exception.CuentaNoVerificadaException;
import co.edu.unbosque.reventacero.exception.ReglaNegocioException;
import co.edu.unbosque.reventacero.repository.UsuarioRepository;
import co.edu.unbosque.reventacero.security.JwtUtil;
import co.edu.unbosque.reventacero.service.AutenticacionService.ResultadoVerificacion;

class AutenticacionServiceTest {

	private UsuarioRepository usuarioRepo;

	private PasswordEncoder passwordEncoder;

	private JwtUtil jwtUtil;

	private CorreoService correoService;

	private AutenticacionService service;

	@BeforeEach
	void setUp() {
		usuarioRepo = mock(UsuarioRepository.class);
		passwordEncoder = new BCryptPasswordEncoder();
		jwtUtil = new JwtUtil();
		ReflectionTestUtils.setField(jwtUtil, "secret", "secreto-de-prueba-reventacero-con-mas-de-32-caracteres");
		correoService = mock(CorreoService.class);
		when(correoService.enviarVerificacion(any(), anyString(), anyLong())).thenReturn(true);
		service = new AutenticacionService(usuarioRepo, passwordEncoder, jwtUtil, correoService, 24);

		when(usuarioRepo.findByNombreUsuario(any())).thenReturn(Optional.empty());
		when(usuarioRepo.findByCorreo(any())).thenReturn(Optional.empty());
	}

	private RegistroDTO registroValido() {
		return new RegistroDTO(1001L, "ana", "claveSegura1", "Ana", "Pérez", "ana@correo.com",
				LocalDate.of(2000, 1, 1), List.of("3001234567"));
	}

	private Usuario cuenta(String estado) {
		Usuario usuario = new Usuario();
		usuario.setDocumento(1001L);
		usuario.setNombreUsuario("ana");
		usuario.setCorreo("ana@correo.com");
		usuario.setContrasenia(passwordEncoder.encode("claveSegura1"));
		usuario.setEstadoCuenta(estado);
		return usuario;
	}

	@Test
	void registrarCifraContraseniaYAsignaRolUsuario() {
		Usuario usuario = service.registrar(registroValido());

		assertNotEquals("claveSegura1", usuario.getContrasenia());
		assertTrue(passwordEncoder.matches("claveSegura1", usuario.getContrasenia()));
		assertEquals(TipoUsuario.USUARIO, usuario.getTipoUsuario());
		verify(usuarioRepo).save(usuario);
	}

	@Test
	void registrarDejaLaCuentaPendienteDeVerificacion() {
		Usuario usuario = service.registrar(registroValido());

		assertEquals(AutenticacionService.ESTADO_CUENTA_PENDIENTE, usuario.getEstadoCuenta());
	}

	@Test
	void registrarFallaSiElCorreoYaExiste() {
		when(usuarioRepo.findByCorreo("ana@correo.com")).thenReturn(Optional.of(new Usuario()));

		assertThrows(CorreoYaExisteException.class, () -> service.registrar(registroValido()));
		verify(usuarioRepo, never()).save(any());
	}

	@Test
	void registrarFallaConContraseniaCorta() {
		RegistroDTO dto = registroValido();
		dto.setContrasenia("123");

		assertThrows(IllegalArgumentException.class, () -> service.registrar(dto));
	}

	@Test
	void loginCorrectoYConContraseniaIncorrecta() {
		Usuario guardado = new Usuario();
		guardado.setNombreUsuario("ana");
		guardado.setContrasenia(passwordEncoder.encode("claveSegura1"));
		when(usuarioRepo.findByNombreUsuario("ana")).thenReturn(Optional.of(guardado));

		assertEquals(guardado, service.login(new LoginDTO("ana", "claveSegura1")));
		assertThrows(CredencialesInvalidasException.class, () -> service.login(new LoginDTO("ana", "otraClave")));
	}

	@Test
	void loginDeCuentaPendienteEsRechazadoSoloConLaContraseniaCorrecta() {
		when(usuarioRepo.findByNombreUsuario("ana")).thenReturn(Optional.of(cuenta("PENDIENTE")));

		assertThrows(CuentaNoVerificadaException.class, () -> service.login(new LoginDTO("ana", "claveSegura1")));
		assertThrows(CredencialesInvalidasException.class, () -> service.login(new LoginDTO("ana", "otraClave")));
	}

	@Test
	void elEnlaceDeVerificacionActivaLaCuenta() {
		Usuario pendiente = cuenta("PENDIENTE");
		when(usuarioRepo.findByDocumento(1001L)).thenReturn(Optional.of(pendiente));
		String token = jwtUtil.generarTokenVerificacion(pendiente, 24);

		assertEquals(ResultadoVerificacion.VERIFICADA, service.verificarCorreo(token));
		verify(usuarioRepo).actualizarEstado(1001L, "ACTIVA");
	}

	@Test
	void abrirElEnlaceDeNuevoNoFallaNiVuelveAActualizar() {
		Usuario activa = cuenta("ACTIVA");
		when(usuarioRepo.findByDocumento(1001L)).thenReturn(Optional.of(activa));
		String token = jwtUtil.generarTokenVerificacion(activa, 24);

		assertEquals(ResultadoVerificacion.YA_VERIFICADA, service.verificarCorreo(token));
		verify(usuarioRepo, never()).actualizarEstado(anyLong(), anyString());
	}

	@Test
	void enlaceAlteradoVencidoODeOtroCorreoEsRechazado() {
		Usuario pendiente = cuenta("PENDIENTE");
		when(usuarioRepo.findByDocumento(1001L)).thenReturn(Optional.of(pendiente));

		String valido = jwtUtil.generarTokenVerificacion(pendiente, 24);
		String vencido = jwtUtil.generarTokenVerificacion(pendiente, -1);

		Usuario otroCorreo = cuenta("PENDIENTE");
		otroCorreo.setCorreo("viejo@correo.com");
		String deOtroCorreo = jwtUtil.generarTokenVerificacion(otroCorreo, 24);

		assertThrows(IllegalArgumentException.class, () -> service.verificarCorreo(valido + "x"));
		assertThrows(IllegalArgumentException.class, () -> service.verificarCorreo(vencido));
		assertThrows(IllegalArgumentException.class, () -> service.verificarCorreo(deOtroCorreo));
		assertThrows(IllegalArgumentException.class, () -> service.verificarCorreo(""));
		verify(usuarioRepo, never()).actualizarEstado(anyLong(), anyString());
	}

	@Test
	void unTokenDeSesionNoSirveComoEnlaceDeVerificacion() {
		Usuario pendiente = cuenta("PENDIENTE");
		pendiente.setTipoUsuario(TipoUsuario.USUARIO);
		when(usuarioRepo.findByDocumento(1001L)).thenReturn(Optional.of(pendiente));

		String tokenSesion = jwtUtil.generateToken(pendiente);

		assertThrows(IllegalArgumentException.class, () -> service.verificarCorreo(tokenSesion));
	}

	@Test
	void unaCuentaSuspendidaNoSeReactivaConElEnlace() {
		Usuario suspendida = cuenta("SUSPENDIDA");
		when(usuarioRepo.findByDocumento(1001L)).thenReturn(Optional.of(suspendida));
		String token = jwtUtil.generarTokenVerificacion(suspendida, 24);

		assertThrows(ReglaNegocioException.class, () -> service.verificarCorreo(token));
		verify(usuarioRepo, never()).actualizarEstado(anyLong(), anyString());
	}

	@Test
	void reenviarSoloEnviaACuentasPendientesYRespetaLaEspera() {
		when(usuarioRepo.findByCorreo("ana@correo.com")).thenReturn(Optional.of(cuenta("PENDIENTE")));
		when(usuarioRepo.findByNombreUsuario("activa")).thenReturn(Optional.of(cuenta("ACTIVA")));

		service.reenviarVerificacion("ana@correo.com");
		service.reenviarVerificacion("ana@correo.com");
		service.reenviarVerificacion("activa");
		service.reenviarVerificacion("noexiste@correo.com");

		verify(correoService, times(1)).enviarVerificacion(any(), anyString(), eq(24L));
	}

}
