package co.edu.unbosque.reventacero.entity;

import java.util.Objects;

public class Patrocinador {

	private long idPatrocinador;
	private String nombre;
	private String tipo;
	private String contacto;
	
	
	public Patrocinador() {
		// TODO Auto-generated constructor stub
	}


	public Patrocinador(long idPatrocinador, String nombre, String tipo, String contacto) {
		super();
		this.idPatrocinador = idPatrocinador;
		this.nombre = nombre;
		this.tipo = tipo;
		this.contacto = contacto;
	}


	public long getIdPatrocinador() {
		return idPatrocinador;
	}


	public void setIdPatrocinador(long idPatrocinador) {
		this.idPatrocinador = idPatrocinador;
	}


	public String getNombre() {
		return nombre;
	}


	public void setNombre(String nombre) {
		this.nombre = nombre;
	}


	public String getTipo() {
		return tipo;
	}


	public void setTipo(String tipo) {
		this.tipo = tipo;
	}


	public String getContacto() {
		return contacto;
	}


	public void setContacto(String contacto) {
		this.contacto = contacto;
	}


	@Override
	public int hashCode() {
		return Objects.hash(contacto, idPatrocinador, nombre, tipo);
	}


	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		Patrocinador other = (Patrocinador) obj;
		return Objects.equals(contacto, other.contacto) && idPatrocinador == other.idPatrocinador
				&& Objects.equals(nombre, other.nombre) && Objects.equals(tipo, other.tipo);
	}


	@Override
	public String toString() {
		return "Patrocinador [idPatrocinador=" + idPatrocinador + ", nombre=" + nombre + ", tipo=" + tipo
				+ ", contacto=" + contacto + "]";
	}
	
	
	
	
}
