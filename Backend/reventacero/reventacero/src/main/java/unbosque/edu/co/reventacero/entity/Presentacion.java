package unbosque.edu.co.reventacero.entity;

import java.time.LocalDate;

public class Presentacion {

	private int idPresentacion;
	private LocalDate fecha;
	private String horaInicio;
	private String horaFin;
	private int idFestival;
	private String nombreEscenario;
	private int idArtista;
	
	
	public Presentacion() {
		// TODO Auto-generated constructor stub
	}


	public Presentacion(int idPresentacion, LocalDate fecha, String horaInicio, String horaFin, int idFestival,
			String nombreEscenario, int idArtista) {
		super();
		this.idPresentacion = idPresentacion;
		this.fecha = fecha;
		this.horaInicio = horaInicio;
		this.horaFin = horaFin;
		this.idFestival = idFestival;
		this.nombreEscenario = nombreEscenario;
		this.idArtista = idArtista;
	}


	public int getIdPresentacion() {
		return idPresentacion;
	}


	public void setIdPresentacion(int idPresentacion) {
		this.idPresentacion = idPresentacion;
	}


	public LocalDate getFecha() {
		return fecha;
	}


	public void setFecha(LocalDate fecha) {
		this.fecha = fecha;
	}


	public String getHoraInicio() {
		return horaInicio;
	}


	public void setHoraInicio(String horaInicio) {
		this.horaInicio = horaInicio;
	}


	public String getHoraFin() {
		return horaFin;
	}


	public void setHoraFin(String horaFin) {
		this.horaFin = horaFin;
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


	public int getIdArtista() {
		return idArtista;
	}


	public void setIdArtista(int idArtista) {
		this.idArtista = idArtista;
	}
	
	
	
	
	
	
}
