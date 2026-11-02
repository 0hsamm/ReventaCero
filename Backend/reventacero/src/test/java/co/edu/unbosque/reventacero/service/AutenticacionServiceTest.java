package co.edu.unbosque.reventacero.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

import co.edu.unbosque.reventacero.dto.LoginDTO;
import co.edu.unbosque.reventacero.dto.RegistroDTO;
import co.edu.unbosque.reventacero.entity.Usuario;
import co.edu.unbosque.reventacero.enums.TipoUsuario;
import co.edu.unbosque.reventacero.exception.CorreoYaExisteException;
import co.edu.unbosque.reventacero.exception.CredencialesInvalidasException;
import co.edu.unbosque.reventacero.repository.UsuarioRepository;

class AutenticacionServiceTest {

	private UsuarioRepository usuarioRepo;

	private PasswordEncoder passwordEncoder;

	private AutenticacionService service;

	@BeforeEach
	void setUp() {
		usuarioRepo = mock(UsuarioRepository.class);
		passwordEncoder = new BCryptPasswordEncoder();
		service = new AutenticacionService(usuarioRepo, passwordEncoder);

		when(usuarioRepo.findByNombreUsuario(any())).thenReturn(Optional.empty());
		when(usuarioRepo.findByCorreo(any())).thenReturn(Optional.empty());
	}

	private RegistroDTO registroValido() {
		return new RegistroDTO(1001L, "ana", "claveSegura1", "Ana", "Pérez", "ana@correo.com",
				LocalDate.of(2000, 1, 1), List.of("3001234567"));
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

}
