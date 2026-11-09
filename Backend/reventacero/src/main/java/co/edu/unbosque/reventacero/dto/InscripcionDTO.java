package co.edu.unbosque.reventacero.dto;

import java.time.LocalDate;

public class InscripcionDTO {

	private long documento;
	private long idLista;
	private long idTipoEntrada;
	private LocalDate fechaSolicitud;
	private String estado;

	public InscripcionDTO() {
	}

	public long getDocumento() {
		return documento;
	}

	public void setDocumento(long documento) {
		this.documento = documento;
	}

	public long getIdLista() {
		return idLista;
	}

	public void setIdLista(long idLista) {
		this.idLista = idLista;
	}

	public long getIdTipoEntrada() {
		return idTipoEntrada;
	}

	public void setIdTipoEntrada(long idTipoEntrada) {
		this.idTipoEntrada = idTipoEntrada;
	}

	public LocalDate getFechaSolicitud() {
		return fechaSolicitud;
	}

	public void setFechaSolicitud(LocalDate fechaSolicitud) {
		this.fechaSolicitud = fechaSolicitud;
	}

	public String getEstado() {
		return estado;
	}

	public void setEstado(String estado) {
		this.estado = estado;
	}
}