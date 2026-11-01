package unbosque.edu.co.reventacero.entity;

import java.time.LocalDate;
import java.util.List;
import java.util.Objects;

import unbosque.edu.co.reventacero.enums.TipoUsuario;

public class Administrador extends Usuario{

	
	private String nivelAcceso;
	private LocalDate fechaDesignacion;
	
	
	public Administrador() {
		// TODO Auto-generated constructor stub
	}


	public Administrador(String nivelAcceso, LocalDate fechaDesignacion) {
		super();
		this.nivelAcceso = nivelAcceso;
		this.fechaDesignacion = fechaDesignacion;
	}


	public Administrador(Long documento, String nombreUsuario, String contrasenia, LocalDate fechaRegistro,
			String estadoCuenta, NombreCompleto nombreCompleto, String correo, LocalDate fechaNacimiento,
			Administrador autorizadoPor, LocalDate fechaAutorizacion, TipoUsuario tipoUsuario, List<String> telefono,
			String nivelAcceso, LocalDate fechaDesignacion) {
		super(documento, nombreUsuario, contrasenia, fechaRegistro, estadoCuenta, nombreCompleto, correo,
				fechaNacimiento, autorizadoPor, fechaAutorizacion, tipoUsuario, telefono);
		this.nivelAcceso = nivelAcceso;
		this.fechaDesignacion = fechaDesignacion;
	}


	public Administrador(Long documento, String nombreUsuario, String contrasenia, LocalDate fechaRegistro,
			String estadoCuenta, NombreCompleto nombreCompleto, String correo, LocalDate fechaNacimiento,
			Administrador autorizadoPor, LocalDate fechaAutorizacion, TipoUsuario tipoUsuario, List<String> telefono) {
		super(documento, nombreUsuario, contrasenia, fechaRegistro, estadoCuenta, nombreCompleto, correo, fechaNacimiento,
				autorizadoPor, fechaAutorizacion, tipoUsuario, telefono);
		// TODO Auto-generated constructor stub
	}


	public String getNivelAcceso() {
		return nivelAcceso;
	}


	public void setNivelAcceso(String nivelAcceso) {
		this.nivelAcceso = nivelAcceso;
	}


	public LocalDate getFechaDesignacion() {
		return fechaDesignacion;
	}


	public void setFechaDesignacion(LocalDate fechaDesignacion) {
		this.fechaDesignacion = fechaDesignacion;
	}


	@Override
	public int hashCode() {
		final int prime = 31;
		int result = super.hashCode();
		result = prime * result + Objects.hash(fechaDesignacion, nivelAcceso);
		return result;
	}


	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (!super.equals(obj))
			return false;
		if (getClass() != obj.getClass())
			return false;
		Administrador other = (Administrador) obj;
		return Objects.equals(fechaDesignacion, other.fechaDesignacion)
				&& Objects.equals(nivelAcceso, other.nivelAcceso);
	}


	@Override
	public String toString() {
		return super.toString()+ "Administrador [nivelAcceso=" + nivelAcceso + ", fechaDesignacion=" + fechaDesignacion + "]";
	}
	
	
	
	
	
	
	
	
}
