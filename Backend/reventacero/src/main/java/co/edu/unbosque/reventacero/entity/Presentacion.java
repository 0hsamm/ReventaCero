package co.edu.unbosque.reventacero.entity;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Objects;

public class Presentacion {

	
	private long idPresentacion;
	private LocalDate fecha;
	private LocalDateTime horaInicio;
	private LocalDateTime horaFin;
	private Escenario escenario;
	private Artista artista;
	
	
	public Presentacion() {
		// TODO Auto-generated constructor stub
	}


	public Presentacion(long idPresentacion, LocalDate fecha, LocalDateTime horaInicio, LocalDateTime horaFin,
			Escenario escenario, Artista artista) {
		super();
		this.idPresentacion = idPresentacion;
		this.fecha = fecha;
		this.horaInicio = horaInicio;
		this.horaFin = horaFin;
		this.escenario = escenario;
		this.artista = artista;
	}


	public long getIdPresentacion() {
		return idPresentacion;
	}


	public void setIdPresentacion(long idPresentacion) {
		this.idPresentacion = idPresentacion;
	}


	public LocalDate getFecha() {
		return fecha;
	}


	public void setFecha(LocalDate fecha) {
		this.fecha = fecha;
	}


	public LocalDateTime getHoraInicio() {
		return horaInicio;
	}


	public void setHoraInicio(LocalDateTime horaInicio) {
		this.horaInicio = horaInicio;
	}


	public LocalDateTime getHoraFin() {
		return horaFin;
	}


	public void setHoraFin(LocalDateTime horaFin) {
		this.horaFin = horaFin;
	}


	public Escenario getEscenario() {
		return escenario;
	}


	public void setEscenario(Escenario escenario) {
		this.escenario = escenario;
	}


	public Artista getArtista() {
		return artista;
	}


	public void setArtista(Artista artista) {
		this.artista = artista;
	}


	@Override
	public int hashCode() {
		return Objects.hash(artista, escenario, fecha, horaFin, horaInicio, idPresentacion);
	}


	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		Presentacion other = (Presentacion) obj;
		return Objects.equals(artista, other.artista) && Objects.equals(escenario, other.escenario)
				&& Objects.equals(fecha, other.fecha) && Objects.equals(horaFin, other.horaFin)
				&& Objects.equals(horaInicio, other.horaInicio) && idPresentacion == other.idPresentacion;
	}


	@Override
	public String toString() {
		return "Presentacion [idPresentacion=" + idPresentacion + ", fecha=" + fecha + ", horaInicio=" + horaInicio
				+ ", horaFin=" + horaFin + ", escenario=" + escenario + ", artista=" + artista + "]";
	}
	
	
	
	
}
