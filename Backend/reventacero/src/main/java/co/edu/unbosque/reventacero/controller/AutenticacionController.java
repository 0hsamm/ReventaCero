package co.edu.unbosque.reventacero.controller;

import java.util.Map;

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
import co.edu.unbosque.reventacero.exception.CuentaNoVerificadaException;
import co.edu.unbosque.reventacero.exception.DocumentoYaExisteException;
import co.edu.unbosque.reventacero.exception.UsuarioYaExisteException;
import co.edu.unbosque.reventacero.security.JwtUtil;
import co.edu.unbosque.reventacero.service.AutenticacionService;
import co.edu.unbosque.reventacero.service.AutenticacionService.ResultadoVerificacion;

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
			Usuario usuario = authService.registrar(registroRequest);

			boolean enviado = authService.enviarCorreoVerificacion(usuario);

			String mensaje = enviado
					? "Cuenta creada. Te enviamos un enlace a " + usuario.getCorreo() + " para verificarla."
					: "Cuenta creada, pero no pudimos enviar el correo de verificación. "
							+ "Pide uno nuevo desde la página de verificación.";

			return ResponseEntity
					.status(HttpStatus.CREATED)
					.body(mensaje);

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

	@PostMapping("/verificar-correo")
	public ResponseEntity<String> verificarCorreo(@RequestBody Map<String, String> cuerpo) {

		ResultadoVerificacion resultado = authService.verificarCorreo(cuerpo.get("token"));

		return ResponseEntity.ok(resultado == ResultadoVerificacion.VERIFICADA
				? "¡Listo! Tu correo quedó verificado. Ya puedes iniciar sesión."
				: "Tu correo ya estaba verificado. Puedes iniciar sesión.");
	}

	@PostMapping("/reenviar-verificacion")
	public ResponseEntity<String> reenviarVerificacion(@RequestBody Map<String, String> cuerpo) {

		authService.reenviarVerificacion(cuerpo.get("usuario"));

		return ResponseEntity.ok("Si hay una cuenta pendiente con esos datos, te enviamos un nuevo enlace. "
				+ "Revisa también la carpeta de spam.");
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

		} catch (CuentaNoVerificadaException e) {
			return ResponseEntity
					.status(HttpStatus.FORBIDDEN)
					.body(e.getMessage());
		}
	}

}
