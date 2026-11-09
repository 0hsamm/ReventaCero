package co.edu.unbosque.reventacero.dto;

public class LoginDTO {

	private String nombreUsuario;
	private String contrasenia;

	public LoginDTO() {
		// TODO Auto-generated constructor stub
	}

	public LoginDTO(String nombreUsuario, String contrasenia) {
		super();
		this.nombreUsuario = nombreUsuario;
		this.contrasenia = contrasenia;
	}

	public String getNombreUsuario() {
		return nombreUsuario;
	}

	public void setNombreUsuario(String nombreUsuario) {
		this.nombreUsuario = nombreUsuario;
	}

	public String getContrasenia() {
		return contrasenia;
	}

	public void setContrasenia(String contrasenia) {
		this.contrasenia = contrasenia;
	}

}
