package co.edu.unbosque.reventacero.dto;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class UsuarioDTO {

	private long documento;
	private String nombreUsuario;
	private String contrasenia;
	private String nombre;
	private String apellido;
	private String correo;
	private LocalDate fechaNacimiento;
	private LocalDate fechaRegistro;
	private String estadoCuenta;
	private String tipoUsuario;
	private List<String> telefono = new ArrayList<>();
	private Long autorizadoPorDocumento;
	private LocalDate fechaAutorizacion;

	public UsuarioDTO() {
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

	public LocalDate getFechaRegistro() {
		return fechaRegistro;
	}

	public void setFechaRegistro(LocalDate fechaRegistro) {
		this.fechaRegistro = fechaRegistro;
	}

	public String getEstadoCuenta() {
		return estadoCuenta;
	}

	public void setEstadoCuenta(String estadoCuenta) {
		this.estadoCuenta = estadoCuenta;
	}

	public String getTipoUsuario() {
		return tipoUsuario;
	}

	public void setTipoUsuario(String tipoUsuario) {
		this.tipoUsuario = tipoUsuario;
	}

	public List<String> getTelefono() {
		return telefono;
	}

	public void setTelefono(List<String> telefono) {
		this.telefono = telefono;
	}

	public Long getAutorizadoPorDocumento() {
		return autorizadoPorDocumento;
	}

	public void setAutorizadoPorDocumento(Long autorizadoPorDocumento) {
		this.autorizadoPorDocumento = autorizadoPorDocumento;
	}

	public LocalDate getFechaAutorizacion() {
		return fechaAutorizacion;
	}

	public void setFechaAutorizacion(LocalDate fechaAutorizacion) {
		this.fechaAutorizacion = fechaAutorizacion;
	}
}