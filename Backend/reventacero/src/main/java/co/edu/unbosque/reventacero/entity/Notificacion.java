package co.edu.unbosque.reventacero.entity;

import java.time.LocalDateTime;
import java.util.Objects;

public class Notificacion {
	
	private long idNotificacion;
	private LocalDateTime fechaHora;
	private String canal;
	private String mensaje;
	private Inscripcion inscripcion;
	
	public Notificacion() {
		// TODO Auto-generated constructor stub
	}

	public Notificacion(long idNotificacion, LocalDateTime fechaHora, String canal, String mensaje,
			Inscripcion inscripcion) {
		super();
		this.idNotificacion = idNotificacion;
		this.fechaHora = fechaHora;
		this.canal = canal;
		this.mensaje = mensaje;
		this.inscripcion = inscripcion;
	}

	public long getIdNotificacion() {
		return idNotificacion;
	}

	public void setIdNotificacion(long idNotificacion) {
		this.idNotificacion = idNotificacion;
	}

	public LocalDateTime getFechaHora() {
		return fechaHora;
	}

	public void setFechaHora(LocalDateTime fechaHora) {
		this.fechaHora = fechaHora;
	}

	public String getCanal() {
		return canal;
	}

	public void setCanal(String canal) {
		this.canal = canal;
	}

	public String getMensaje() {
		return mensaje;
	}

	public void setMensaje(String mensaje) {
		this.mensaje = mensaje;
	}

	public Inscripcion getInscripcion() {
		return inscripcion;
	}

	public void setInscripcion(Inscripcion inscripcion) {
		this.inscripcion = inscripcion;
	}

	@Override
	public int hashCode() {
		return Objects.hash(canal, fechaHora, idNotificacion, inscripcion, mensaje);
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		Notificacion other = (Notificacion) obj;
		return Objects.equals(canal, other.canal) && Objects.equals(fechaHora, other.fechaHora)
				&& idNotificacion == other.idNotificacion && Objects.equals(inscripcion, other.inscripcion)
				&& Objects.equals(mensaje, other.mensaje);
	}

	@Override
	public String toString() {
		return "Notificacion [idNotificacion=" + idNotificacion + ", fechaHora=" + fechaHora + ", canal=" + canal
				+ ", mensaje=" + mensaje + ", inscripcion=" + inscripcion + "]";
	}
	
	
	
	

}
