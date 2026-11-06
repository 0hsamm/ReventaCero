package co.edu.unbosque.reventacero.dto;

public class EscenarioDTO {

	private long idFestival;
	private String nombreEscenario;
	private int capacidad;

	public EscenarioDTO() {
	}

	public long getIdFestival() { return idFestival; }
	public void setIdFestival(long idFestival) { this.idFestival = idFestival; }
	public String getNombreEscenario() { return nombreEscenario; }
	public void setNombreEscenario(String nombreEscenario) { this.nombreEscenario = nombreEscenario; }
	public int getCapacidad() { return capacidad; }
	public void setCapacidad(int capacidad) { this.capacidad = capacidad; }

}