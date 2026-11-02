package co.edu.unbosque.reventacero.dto;

import java.time.LocalDateTime;

public class NotificacionDTO {

	private long idNotificacion;
	private LocalDateTime fechaHora;
	private String canal;
	private String mensaje;
	private long usuarioDocumento;
	private long idLista;

	public NotificacionDTO() {
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

	public long getUsuarioDocumento() {
		return usuarioDocumento;
	}

	public void setUsuarioDocumento(long usuarioDocumento) {
		this.usuarioDocumento = usuarioDocumento;
	}

	public long getIdLista() {
		return idLista;
	}

	public void setIdLista(long idLista) {
		this.idLista = idLista;
	}
}