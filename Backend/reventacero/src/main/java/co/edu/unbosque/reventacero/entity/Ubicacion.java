package co.edu.unbosque.reventacero.entity;

import java.util.Objects;

public class Ubicacion {

	
	private String pais;
	private String ciudad;
	private String direccion;
	
	
	public Ubicacion() {
		// TODO Auto-generated constructor stub
	}


	public Ubicacion(String pais, String ciudad, String direccion) {
		super();
		this.pais = pais;
		this.ciudad = ciudad;
		this.direccion = direccion;
	}


	public String getPais() {
		return pais;
	}


	public void setPais(String pais) {
		this.pais = pais;
	}


	public String getCiudad() {
		return ciudad;
	}


	public void setCiudad(String ciudad) {
		this.ciudad = ciudad;
	}


	public String getDireccion() {
		return direccion;
	}


	public void setDireccion(String direccion) {
		this.direccion = direccion;
	}


	@Override
	public int hashCode() {
		return Objects.hash(ciudad, direccion, pais);
	}


	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		Ubicacion other = (Ubicacion) obj;
		return Objects.equals(ciudad, other.ciudad) && Objects.equals(direccion, other.direccion)
				&& Objects.equals(pais, other.pais);
	}


	@Override
	public String toString() {
		return "Ubicacion [pais=" + pais + ", ciudad=" + ciudad + ", direccion=" + direccion + "]";
	}
	
	
	
	
	
}
