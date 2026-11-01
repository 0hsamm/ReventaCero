package unbosque.edu.co.reventacero.entity;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import unbosque.edu.co.reventacero.enums.TipoUsuario;

public class Usuario {
	
	private long documento;
	private String nombreUsuario;
	private String contrasenia;
	private LocalDate fechaRegistro;
	private String estadoCuenta;
	private NombreCompleto nombreCompleto;
	private String correo;
	private LocalDate fechaNacimiento;
	private Administrador autorizadoPor;
	private LocalDate fechaAutorizacion;
	private TipoUsuario tipoUsuario;
	private List<String> telefono = new ArrayList<>();
	
	public Usuario() {
		// TODO Auto-generated constructor stub
	}

	public Usuario(long documento, String nombreUsuario, String contrasenia, LocalDate fechaRegistro,
			String estadoCuenta, NombreCompleto nombreCompleto, String correo, LocalDate fechaNacimiento,
			Administrador autorizadoPor, LocalDate fechaAutorizacion, TipoUsuario tipoUsuario, List<String> telefono) {
		super();
		this.documento = documento;
		this.nombreUsuario = nombreUsuario;
		this.contrasenia = contrasenia;
		this.fechaRegistro = fechaRegistro;
		this.estadoCuenta = estadoCuenta;
		this.nombreCompleto = nombreCompleto;
		this.correo = correo;
		this.fechaNacimiento = fechaNacimiento;
		this.autorizadoPor = autorizadoPor;
		this.fechaAutorizacion = fechaAutorizacion;
		this.tipoUsuario = tipoUsuario;
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

	public NombreCompleto getNombreCompleto() {
		return nombreCompleto;
	}

	public void setNombreCompleto(NombreCompleto nombreCompleto) {
		this.nombreCompleto = nombreCompleto;
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

	public Administrador getAutorizadoPor() {
		return autorizadoPor;
	}

	public void setAutorizadoPor(Administrador autorizadoPor) {
		this.autorizadoPor = autorizadoPor;
	}

	public LocalDate getFechaAutorizacion() {
		return fechaAutorizacion;
	}

	public void setFechaAutorizacion(LocalDate fechaAutorizacion) {
		this.fechaAutorizacion = fechaAutorizacion;
	}

	public TipoUsuario getTipoUsuario() {
		return tipoUsuario;
	}

	public void setTipoUsuario(TipoUsuario tipoUsuario) {
		this.tipoUsuario = tipoUsuario;
	}

	public List<String> getTelefono() {
		return telefono;
	}

	public void setTelefono(List<String> telefono) {
		this.telefono = telefono;
	}

	@Override
	public int hashCode() {
		return Objects.hash(autorizadoPor, contrasenia, correo, documento, estadoCuenta, fechaAutorizacion,
				fechaNacimiento, fechaRegistro, nombreCompleto, nombreUsuario, telefono, tipoUsuario);
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		Usuario other = (Usuario) obj;
		return Objects.equals(autorizadoPor, other.autorizadoPor) && Objects.equals(contrasenia, other.contrasenia)
				&& Objects.equals(correo, other.correo) && Objects.equals(documento, other.documento)
				&& Objects.equals(estadoCuenta, other.estadoCuenta)
				&& Objects.equals(fechaAutorizacion, other.fechaAutorizacion)
				&& Objects.equals(fechaNacimiento, other.fechaNacimiento)
				&& Objects.equals(fechaRegistro, other.fechaRegistro)
				&& Objects.equals(nombreCompleto, other.nombreCompleto)
				&& Objects.equals(nombreUsuario, other.nombreUsuario) && Objects.equals(telefono, other.telefono)
				&& tipoUsuario == other.tipoUsuario;
	}

	@Override
	public String toString() {
		return "Usuario [documento=" + documento + ", nombreUsuario=" + nombreUsuario + ", contrasenia=" + contrasenia
				+ ", fechaRegistro=" + fechaRegistro + ", estadoCuenta=" + estadoCuenta + ", nombreCompleto="
				+ nombreCompleto + ", correo=" + correo + ", fechaNacimiento=" + fechaNacimiento + ", autorizadoPor="
				+ autorizadoPor + ", fechaAutorizacion=" + fechaAutorizacion + ", tipoUsuario=" + tipoUsuario
				+ ", telefono=" + telefono + "]";
	}
	
	
	
	
	

}
