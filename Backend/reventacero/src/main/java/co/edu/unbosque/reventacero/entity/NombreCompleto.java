package co.edu.unbosque.reventacero.entity;

import java.util.Objects;

public class NombreCompleto {

	private String nombre;
	private String apellido;
	
	
	public NombreCompleto() {
		// TODO Auto-generated constructor stub
	}


	public NombreCompleto(String nombre, String apellido) {
		super();
		this.nombre = nombre;
		this.apellido = apellido;
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


	@Override
	public int hashCode() {
		return Objects.hash(apellido, nombre);
	}


	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		NombreCompleto other = (NombreCompleto) obj;
		return Objects.equals(apellido, other.apellido) && Objects.equals(nombre, other.nombre);
	}


	@Override
	public String toString() {
		return "NombreCompleto [nombre=" + nombre + ", apellido=" + apellido + "]";
	}
	
	
	
	
}
