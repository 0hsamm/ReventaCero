package co.edu.unbosque.reventacero.dto;

import java.time.LocalDate;

public class BoletaDTO {

	private long codigoBoleta;
	private LocalDate fechaEmision;
	private long idTipoEntrada;
	private long documentoComprador;

	public BoletaDTO() {
	}

	public long getCodigoBoleta() {
		return codigoBoleta;
	}

	public void setCodigoBoleta(long codigoBoleta) {
		this.codigoBoleta = codigoBoleta;
	}

	public LocalDate getFechaEmision() {
		return fechaEmision;
	}

	public void setFechaEmision(LocalDate fechaEmision) {
		this.fechaEmision = fechaEmision;
	}

	public long getIdTipoEntrada() {
		return idTipoEntrada;
	}

	public void setIdTipoEntrada(long idTipoEntrada) {
		this.idTipoEntrada = idTipoEntrada;
	}

	public long getDocumentoComprador() {
		return documentoComprador;
	}

	public void setDocumentoComprador(long documentoComprador) {
		this.documentoComprador = documentoComprador;
	}
}