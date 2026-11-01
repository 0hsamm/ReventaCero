package co.edu.unbosque.reventacero.entity;

import java.util.Objects;

public class Escenario {

	
	private Festival festival;
	private String nombreEscenario;
	private int capacidad;
	
	
	public Escenario() {
		// TODO Auto-generated constructor stub
	}


	public Escenario(Festival festival, String nombreEscenario, int capacidad) {
		super();
		this.festival = festival;
		this.nombreEscenario = nombreEscenario;
		this.capacidad = capacidad;
	}


	public Festival getFestival() {
		return festival;
	}


	public void setFestival(Festival festival) {
		this.festival = festival;
	}


	public String getNombreEscenario() {
		return nombreEscenario;
	}


	public void setNombreEscenario(String nombreEscenario) {
		this.nombreEscenario = nombreEscenario;
	}


	public int getCapacidad() {
		return capacidad;
	}


	public void setCapacidad(int capacidad) {
		this.capacidad = capacidad;
	}


	@Override
	public int hashCode() {
		return Objects.hash(capacidad, festival, nombreEscenario);
	}


	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		Escenario other = (Escenario) obj;
		return capacidad == other.capacidad && Objects.equals(festival, other.festival)
				&& Objects.equals(nombreEscenario, other.nombreEscenario);
	}


	@Override
	public String toString() {
		return "Escenario [festival=" + festival + ", nombreEscenario=" + nombreEscenario + ", capacidad=" + capacidad
				+ "]";
	}
	
	
	
	
}
