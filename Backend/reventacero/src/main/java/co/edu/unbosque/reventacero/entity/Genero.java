package co.edu.unbosque.reventacero.entity;

import java.util.Objects;

public class Genero {

	
	private long idGenero;
	private String nombre;
	private Genero generoPrincipal;
	
	
	public Genero() {
		// TODO Auto-generated constructor stub
	}


	public Genero(long idGenero, String nombre, Genero generoPrincipal) {
		super();
		this.idGenero = idGenero;
		this.nombre = nombre;
		this.generoPrincipal = generoPrincipal;
	}


	public long getIdGenero() {
		return idGenero;
	}


	public void setIdGenero(long idGenero) {
		this.idGenero = idGenero;
	}


	public String getNombre() {
		return nombre;
	}


	public void setNombre(String nombre) {
		this.nombre = nombre;
	}


	public Genero getGeneroPrincipal() {
		return generoPrincipal;
	}


	public void setGeneroPrincipal(Genero generoPrincipal) {
		this.generoPrincipal = generoPrincipal;
	}


	@Override
	public int hashCode() {
		return Objects.hash(generoPrincipal, idGenero, nombre);
	}


	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		Genero other = (Genero) obj;
		return Objects.equals(generoPrincipal, other.generoPrincipal) && Objects.equals(idGenero, other.idGenero)
				&& Objects.equals(nombre, other.nombre);
	}


	@Override
	public String toString() {
		return "Genero [idGenero=" + idGenero + ", nombre=" + nombre + ", generoPrincipal=" + generoPrincipal + "]";
	}
	
	
	
	
	
}
