package unbosque.edu.co.reventacero.entity;

import java.util.Objects;

public class Zona {

	
	private long idZona;
	private String nombre;
	private String tipoZona;
	private int aforoMaximo;
	private Festival festival;
	
	
	public Zona() {
		// TODO Auto-generated constructor stub
	}


	public Zona(long idZona, String nombre, String tipoZona, int aforoMaximo, Festival festival) {
		super();
		this.idZona = idZona;
		this.nombre = nombre;
		this.tipoZona = tipoZona;
		this.aforoMaximo = aforoMaximo;
		this.festival = festival;
	}


	public long getIdZona() {
		return idZona;
	}


	public void setIdZona(long idZona) {
		this.idZona = idZona;
	}


	public String getNombre() {
		return nombre;
	}


	public void setNombre(String nombre) {
		this.nombre = nombre;
	}


	public String getTipoZona() {
		return tipoZona;
	}


	public void setTipoZona(String tipoZona) {
		this.tipoZona = tipoZona;
	}


	public int getAforoMaximo() {
		return aforoMaximo;
	}


	public void setAforoMaximo(int aforoMaximo) {
		this.aforoMaximo = aforoMaximo;
	}


	public Festival getFestival() {
		return festival;
	}


	public void setFestival(Festival festival) {
		this.festival = festival;
	}


	@Override
	public int hashCode() {
		return Objects.hash(aforoMaximo, festival, idZona, nombre, tipoZona);
	}


	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		Zona other = (Zona) obj;
		return aforoMaximo == other.aforoMaximo && Objects.equals(festival, other.festival) && idZona == other.idZona
				&& Objects.equals(nombre, other.nombre) && Objects.equals(tipoZona, other.tipoZona);
	}


	@Override
	public String toString() {
		return "Zona [idZona=" + idZona + ", nombre=" + nombre + ", tipoZona=" + tipoZona + ", aforoMaximo="
				+ aforoMaximo + ", festival=" + festival + "]";
	}
	
	
	
	
}
