package co.edu.unbosque.reventacero.dto;

/**
 * Respuesta del login.
 * Incluye el JWT que el frontend debe enviar como Bearer token.
 */
public class RespuestaAutenticacionDTO {

	private String token;
	private String tipoToken;
	private long documento;
	private String nombreUsuario;
	private String correo;
	private String tipoUsuario;

	public RespuestaAutenticacionDTO() {
		// TODO Auto-generated constructor stub
	}

	public RespuestaAutenticacionDTO(String token, String tipoToken, long documento, String nombreUsuario,
			String correo, String tipoUsuario) {
		super();
		this.token = token;
		this.tipoToken = tipoToken;
		this.documento = documento;
		this.nombreUsuario = nombreUsuario;
		this.correo = correo;
		this.tipoUsuario = tipoUsuario;
	}

	public String getToken() {
		return token;
	}

	public void setToken(String token) {
		this.token = token;
	}

	public String getTipoToken() {
		return tipoToken;
	}

	public void setTipoToken(String tipoToken) {
		this.tipoToken = tipoToken;
	}

	public long getDocumento() {
		return documento;
	}

	public void setDocumento(long documento) {
		this.documento = documento;
	}

	public String getNombreUsuario() {
		return nombreUsuario;
	}

	public void setNombreUsuario(String nombreUsuario) {
		this.nombreUsuario = nombreUsuario;
	}

	public String getCorreo() {
		return correo;
	}

	public void setCorreo(String correo) {
		this.correo = correo;
	}

	public String getTipoUsuario() {
		return tipoUsuario;
	}

	public void setTipoUsuario(String tipoUsuario) {
		this.tipoUsuario = tipoUsuario;
	}

}
