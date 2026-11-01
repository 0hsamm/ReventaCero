package unbosque.edu.co.reventacero.entity;

import java.time.LocalDateTime;

public class SeInscribe {

	private int idLista;
	private String documento;
	private LocalDateTime fechaSolicitud;
	private String estado;
	
	public SeInscribe() {
		// TODO Auto-generated constructor stub
	}

	public SeInscribe(int idLista, String documento, LocalDateTime fechaSolicitud, String estado) {
		super();
		this.idLista = idLista;
		this.documento = documento;
		this.fechaSolicitud = fechaSolicitud;
		this.estado = estado;
	}

	public int getIdLista() {
		return idLista;
	}

	public void setIdLista(int idLista) {
		this.idLista = idLista;
	}

	public String getDocumento() {
		return documento;
	}

	public void setDocumento(String documento) {
		this.documento = documento;
	}

	public LocalDateTime getFechaSolicitud() {
		return fechaSolicitud;
	}

	public void setFechaSolicitud(LocalDateTime fechaSolicitud) {
		this.fechaSolicitud = fechaSolicitud;
	}

	public String getEstado() {
		return estado;
	}

	public void setEstado(String estado) {
		this.estado = estado;
	}
	
	
	
}
