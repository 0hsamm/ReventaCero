package co.edu.unbosque.reventacero.dto;

import java.time.LocalDate;
import java.time.LocalDateTime;

public class PresentacionDTO {

	private long idPresentacion;
	private LocalDate fecha;
	private LocalDateTime horaInicio;
	private LocalDateTime horaFin;
	private long idFestival;
	private String nombreEscenario;
	private long idArtista;

	public PresentacionDTO() {
	}

	public long getIdPresentacion() { return idPresentacion; }
	public void setIdPresentacion(long idPresentacion) { this.idPresentacion = idPresentacion; }
	public LocalDate getFecha() { return fecha; }
	public void setFecha(LocalDate fecha) { this.fecha = fecha; }
	public LocalDateTime getHoraInicio() { return horaInicio; }
	public void setHoraInicio(LocalDateTime horaInicio) { this.horaInicio = horaInicio; }
	public LocalDateTime getHoraFin() { return horaFin; }
	public void setHoraFin(LocalDateTime horaFin) { this.horaFin = horaFin; }
	public long getIdFestival() { return idFestival; }
	public void setIdFestival(long idFestival) { this.idFestival = idFestival; }
	public String getNombreEscenario() { return nombreEscenario; }
	public void setNombreEscenario(String nombreEscenario) { this.nombreEscenario = nombreEscenario; }
	public long getIdArtista() { return idArtista; }
	public void setIdArtista(long idArtista) { this.idArtista = idArtista; }

}