package co.edu.unbosque.reventacero.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import co.edu.unbosque.reventacero.dto.LoginDTO;
import co.edu.unbosque.reventacero.dto.RegistroDTO;
import co.edu.unbosque.reventacero.dto.RespuestaAutenticacionDTO;
import co.edu.unbosque.reventacero.entity.Usuario;
import co.edu.unbosque.reventacero.exception.CorreoYaExisteException;
import co.edu.unbosque.reventacero.exception.CredencialesInvalidasException;
import co.edu.unbosque.reventacero.exception.DocumentoYaExisteException;
import co.edu.unbosque.reventacero.exception.UsuarioYaExisteException;
import co.edu.unbosque.reventacero.security.JwtUtil;
import co.edu.unbosque.reventacero.service.AutenticacionService;

/**
 * Controlador encargado del registro
 * y el inicio de sesión de usuarios.
 */
@RestController
@RequestMapping("/auth")
public class AutenticacionController {

	private final AutenticacionService authService;

	private final JwtUtil jwtUtil;

	public AutenticacionController(AutenticacionService authService, JwtUtil jwtUtil) {
		this.authService = authService;
		this.jwtUtil = jwtUtil;
	}

	@PostMapping("/registro")
	public ResponseEntity<?> registro(@RequestBody RegistroDTO registroRequest) {

		try {
			authService.registrar(registroRequest);

			return ResponseEntity
					.status(HttpStatus.CREATED)
					.body("Usuario registrado correctamente");

		} catch (DocumentoYaExisteException | UsuarioYaExisteException | CorreoYaExisteException e) {
			return ResponseEntity
					.status(HttpStatus.CONFLICT)
					.body(e.getMessage());

		} catch (IllegalArgumentException e) {
			return ResponseEntity
					.status(HttpStatus.BAD_REQUEST)
					.body(e.getMessage());
		}
	}

	@PostMapping("/login")
	public ResponseEntity<?> login(@RequestBody LoginDTO loginRequest) {

		try {
			Usuario usuarioLogeado = authService.login(loginRequest);

			String token = jwtUtil.generateToken(usuarioLogeado);

			RespuestaAutenticacionDTO respuesta = new RespuestaAutenticacionDTO(
					token,
					"Bearer",
					usuarioLogeado.getDocumento(),
					usuarioLogeado.getNombreUsuario(),
					usuarioLogeado.getCorreo(),
					usuarioLogeado.getTipoUsuario() != null ? usuarioLogeado.getTipoUsuario().name() : null);

			return ResponseEntity.ok(respuesta);

		} catch (CredencialesInvalidasException e) {
			return ResponseEntity
					.status(HttpStatus.UNAUTHORIZED)
					.body(e.getMessage());
		}
	}

}
