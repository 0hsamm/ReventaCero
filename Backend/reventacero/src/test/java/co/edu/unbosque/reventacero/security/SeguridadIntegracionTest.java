package co.edu.unbosque.reventacero.security;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import co.edu.unbosque.reventacero.entity.Usuario;
import co.edu.unbosque.reventacero.enums.TipoUsuario;
import co.edu.unbosque.reventacero.repository.UsuarioRepository;

/**
 * Levanta la cadena completa de Spring Security sin Oracle:
 * el repositorio se reemplaza por un mock.
 */
@SpringBootTest(properties = {
		"spring.autoconfigure.exclude="
				+ "org.springframework.boot.jdbc.autoconfigure.DataSourceAutoConfiguration,"
				+ "org.springframework.boot.jdbc.autoconfigure.DataSourceTransactionManagerAutoConfiguration,"
				+ "org.springframework.boot.jdbc.autoconfigure.JdbcClientAutoConfiguration",
		"jwt.secret=secreto-de-prueba-reventacero-con-mas-de-32-caracteres",
		"app.frontend.allowed-origins=http://localhost:4200" })
@AutoConfigureMockMvc
class SeguridadIntegracionTest {

	@Autowired
	private MockMvc mockMvc;

	@Autowired
	private JwtUtil jwtUtil;

	@Autowired
	private PasswordEncoder passwordEncoder;

	@MockitoBean
	private UsuarioRepository usuarioRepository;

	private Usuario usuarioNormal;

	private Usuario administrador;

	@BeforeEach
	void setUp() {
		usuarioNormal = crearUsuario(1L, "ana", "ana@correo.com", TipoUsuario.USUARIO);
		administrador = crearUsuario(2L, "admin", "admin@correo.com", TipoUsuario.ADMINISTRADOR);

		when(usuarioRepository.findByCorreo(any())).thenReturn(Optional.empty());
		when(usuarioRepository.findByNombreUsuario(any())).thenReturn(Optional.empty());
		when(usuarioRepository.findByCorreo("ana@correo.com")).thenReturn(Optional.of(usuarioNormal));
		when(usuarioRepository.findByNombreUsuario("ana")).thenReturn(Optional.of(usuarioNormal));
		when(usuarioRepository.findByCorreo("admin@correo.com")).thenReturn(Optional.of(administrador));
	}

	private Usuario crearUsuario(long documento, String nombreUsuario, String correo, TipoUsuario tipo) {
		Usuario usuario = new Usuario();
		usuario.setDocumento(documento);
		usuario.setNombreUsuario(nombreUsuario);
		usuario.setCorreo(correo);
		usuario.setContrasenia(passwordEncoder.encode("claveSegura1"));
		usuario.setTipoUsuario(tipo);
		return usuario;
	}

	private String bearer(Usuario usuario) {
		return "Bearer " + jwtUtil.generateToken(usuario);
	}

	@Test
	void loginCorrectoDevuelveToken() throws Exception {
		mockMvc.perform(post("/auth/login")
				.contentType(MediaType.APPLICATION_JSON)
				.content("{\"nombreUsuario\":\"ana\",\"contrasenia\":\"claveSegura1\"}"))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.token").isNotEmpty())
				.andExpect(jsonPath("$.tipoUsuario").value("USUARIO"));
	}

	@Test
	void loginConContraseniaIncorrectaDevuelve401() throws Exception {
		mockMvc.perform(post("/auth/login")
				.contentType(MediaType.APPLICATION_JSON)
				.content("{\"nombreUsuario\":\"ana\",\"contrasenia\":\"incorrecta\"}"))
				.andExpect(status().isUnauthorized());
	}

	@Test
	void registroEsPublico() throws Exception {
		mockMvc.perform(post("/auth/registro")
				.contentType(MediaType.APPLICATION_JSON)
				.content("{\"documento\":99,\"nombreUsuario\":\"nuevo\",\"contrasenia\":\"claveSegura1\","
						+ "\"nombre\":\"Juan\",\"apellido\":\"Gómez\",\"correo\":\"nuevo@correo.com\"}"))
				.andExpect(status().isCreated());
	}

	@Test
	void rutaProtegidaSinTokenEsRechazada() throws Exception {
		mockMvc.perform(get("/boletas"))
				.andExpect(status().isUnauthorized());
	}

	@Test
	void rutaProtegidaConTokenInvalidoEsRechazada() throws Exception {
		mockMvc.perform(get("/boletas").header("Authorization", "Bearer token-falso"))
				.andExpect(status().isUnauthorized());
	}

	@Test
	void rutaProtegidaConTokenValidoPasaLaSeguridad() throws Exception {
		// 404 = la seguridad dejó pasar; simplemente aún no existe el controlador
		mockMvc.perform(get("/boletas").header("Authorization", bearer(usuarioNormal)))
				.andExpect(status().isNotFound());
	}

	@Test
	void tokenDeUsuarioQueYaNoExisteEsRechazado() throws Exception {
		Usuario borrado = crearUsuario(3L, "borrado", "borrado@correo.com", TipoUsuario.USUARIO);

		mockMvc.perform(get("/boletas").header("Authorization", bearer(borrado)))
				.andExpect(status().isUnauthorized());
	}

	@Test
	void usuarioNormalNoEntraAAdmin() throws Exception {
		mockMvc.perform(get("/admin/panel").header("Authorization", bearer(usuarioNormal)))
				.andExpect(status().isForbidden());
	}

	@Test
	void administradorSiEntraAAdmin() throws Exception {
		mockMvc.perform(get("/admin/panel").header("Authorization", bearer(administrador)))
				.andExpect(status().isNotFound());
	}

	@Test
	void consultarFestivalesEsPublico() throws Exception {
		mockMvc.perform(get("/festivales"))
				.andExpect(status().isNotFound());
	}

	@Test
	void usuarioNormalNoPuedeCrearFestivales() throws Exception {
		mockMvc.perform(post("/festivales").header("Authorization", bearer(usuarioNormal)))
				.andExpect(status().isForbidden());
	}

}
