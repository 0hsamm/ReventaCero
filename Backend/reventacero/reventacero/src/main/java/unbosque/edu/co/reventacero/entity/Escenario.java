package unbosque.edu.co.reventacero.entity;

public class Escenario {
	
	
	private int idFestival;
	private String nombreEscenario;
	private int capacidad;
	
	public Escenario() {
		// TODO Auto-generated constructor stub
	}

	public Escenario(int idFestival, String nombreEscenario, int capacidad) {
		super();
		this.idFestival = idFestival;
		this.nombreEscenario = nombreEscenario;
		this.capacidad = capacidad;
	}

	public int getIdFestival() {
		return idFestival;
	}

	public void setIdFestival(int idFestival) {
		this.idFestival = idFestival;
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
	
	
	

}
