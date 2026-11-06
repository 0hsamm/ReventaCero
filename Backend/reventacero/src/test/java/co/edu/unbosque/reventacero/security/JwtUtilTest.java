package co.edu.unbosque.reventacero.security;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import co.edu.unbosque.reventacero.entity.Usuario;
import co.edu.unbosque.reventacero.enums.TipoUsuario;

class JwtUtilTest {

	private JwtUtil jwtUtil;

	private Usuario usuario;

	@BeforeEach
	void setUp() {
		jwtUtil = new JwtUtil();
		ReflectionTestUtils.setField(jwtUtil, "secret", "secreto-de-prueba-reventacero-con-mas-de-32-caracteres");

		usuario = new Usuario();
		usuario.setDocumento(1001L);
		usuario.setNombreUsuario("ana");
		usuario.setCorreo("ana@correo.com");
		usuario.setTipoUsuario(TipoUsuario.ORGANIZADOR);
	}

	@Test
	void generaTokenConCorreoComoSubjectYClaimDeRol() {
		String token = jwtUtil.generateToken(usuario);

		assertEquals("ana@correo.com", jwtUtil.extractUsername(token));
		assertEquals("ORGANIZADOR", jwtUtil.extractClaim(token, c -> c.get("tipoUsuario", String.class)));
		assertTrue(jwtUtil.validateToken(token, usuario));
	}

	@Test
	void tokenNoEsValidoParaOtroUsuario() {
		String token = jwtUtil.generateToken(usuario);

		Usuario otro = new Usuario();
		otro.setCorreo("otro@correo.com");

		assertFalse(jwtUtil.validateToken(token, otro));
	}

	@Test
	void rolTienePrefijoRole() {
		assertEquals("ROLE_ORGANIZADOR", usuario.getAuthorities().iterator().next().getAuthority());
	}

}
