package co.edu.unbosque.reventacero.entity;

import java.time.LocalDate;
import java.util.List;
import java.util.Objects;

import co.edu.unbosque.reventacero.enums.TipoUsuario;

public class Organizador extends Usuario{

	
	private String cargo;
	private Organizador supervisor;
	
	public Organizador() {
		// TODO Auto-generated constructor stub
	}

	public Organizador(String cargo, Organizador supervisor) {
		super();
		this.cargo = cargo;
		this.supervisor = supervisor;
	}

	public Organizador(long documento, String nombreUsuario, String contrasenia, LocalDate fechaRegistro,
			String estadoCuenta, NombreCompleto nombreCompleto, String correo, LocalDate fechaNacimiento,
			Administrador autorizadoPor, LocalDate fechaAutorizacion, TipoUsuario tipoUsuario, List<String> telefono,
			String cargo, Organizador supervisor) {
		super(documento, nombreUsuario, contrasenia, fechaRegistro, estadoCuenta, nombreCompleto, correo,
				fechaNacimiento, autorizadoPor, fechaAutorizacion, tipoUsuario, telefono);
		this.cargo = cargo;
		this.supervisor = supervisor;
	}

	public Organizador(long documento, String nombreUsuario, String contrasenia, LocalDate fechaRegistro,
			String estadoCuenta, NombreCompleto nombreCompleto, String correo, LocalDate fechaNacimiento,
			Administrador autorizadoPor, LocalDate fechaAutorizacion, TipoUsuario tipoUsuario, List<String> telefono) {
		super(documento, nombreUsuario, contrasenia, fechaRegistro, estadoCuenta, nombreCompleto, correo, fechaNacimiento,
				autorizadoPor, fechaAutorizacion, tipoUsuario, telefono);
		// TODO Auto-generated constructor stub
	}

	public String getCargo() {
		return cargo;
	}

	public void setCargo(String cargo) {
		this.cargo = cargo;
	}

	public Organizador getSupervisor() {
		return supervisor;
	}

	public void setSupervisor(Organizador supervisor) {
		this.supervisor = supervisor;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = super.hashCode();
		result = prime * result + Objects.hash(cargo, supervisor);
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
		Organizador other = (Organizador) obj;
		return Objects.equals(cargo, other.cargo) && Objects.equals(supervisor, other.supervisor);
	}

	@Override
	public String toString() {
		return super.toString() + "Organizador [cargo=" + cargo + ", supervisor=" + supervisor + "]";
	}
	
	
	
	
}
