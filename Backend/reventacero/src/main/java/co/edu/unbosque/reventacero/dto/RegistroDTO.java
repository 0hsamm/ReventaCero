package co.edu.unbosque.reventacero.dto;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class RegistroDTO {

	private long documento;
	private String nombreUsuario;
	private String contrasenia;
	private String nombre;
	private String apellido;
	private String correo;
	private LocalDate fechaNacimiento;
	private List<String> telefono = new ArrayList<>();

	public RegistroDTO() {
		// TODO Auto-generated constructor stub
	}

	public RegistroDTO(long documento, String nombreUsuario, String contrasenia, String nombre, String apellido,
			String correo, LocalDate fechaNacimiento, List<String> telefono) {
		super();
		this.documento = documento;
		this.nombreUsuario = nombreUsuario;
		this.contrasenia = contrasenia;
		this.nombre = nombre;
		this.apellido = apellido;
		this.correo = correo;
		this.fechaNacimiento = fechaNacimiento;
		this.telefono = telefono;
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

	public String getContrasenia() {
		return contrasenia;
	}

	public void setContrasenia(String contrasenia) {
		this.contrasenia = contrasenia;
	}

	public String getNombre() {
		return nombre;
	}

	public void setNombre(String nombre) {
		this.nombre = nombre;
	}

	public String getApellido() {
		return apellido;
	}

	public void setApellido(String apellido) {
		this.apellido = apellido;
	}

	public String getCorreo() {
		return correo;
	}

	public void setCorreo(String correo) {
		this.correo = correo;
	}

	public LocalDate getFechaNacimiento() {
		return fechaNacimiento;
	}

	public void setFechaNacimiento(LocalDate fechaNacimiento) {
		this.fechaNacimiento = fechaNacimiento;
	}

	public List<String> getTelefono() {
		return telefono;
	}

	public void setTelefono(List<String> telefono) {
		this.telefono = telefono; // skipcq: JAVA-E1086
	}

}
