package unbosque.edu.co.reventacero.entity;

import java.time.LocalDateTime;

public class Boleta {

	
	private String codigoBoleta;
	private LocalDateTime fechaEmision;
	private int idTipoEntrada;
	private String documentoAsistente;
	private int idDispositivo;
	
	
	public Boleta() {
		// TODO Auto-generated constructor stub
	}


	public Boleta(String codigoBoleta, LocalDateTime fechaEmision, int idTipoEntrada, String documentoAsistente,
			int idDispositivo) {
		super();
		this.codigoBoleta = codigoBoleta;
		this.fechaEmision = fechaEmision;
		this.idTipoEntrada = idTipoEntrada;
		this.documentoAsistente = documentoAsistente;
		this.idDispositivo = idDispositivo;
	}


	public String getCodigoBoleta() {
		return codigoBoleta;
	}


	public void setCodigoBoleta(String codigoBoleta) {
		this.codigoBoleta = codigoBoleta;
	}


	public LocalDateTime getFechaEmision() {
		return fechaEmision;
	}


	public void setFechaEmision(LocalDateTime fechaEmision) {
		this.fechaEmision = fechaEmision;
	}


	public int getIdTipoEntrada() {
		return idTipoEntrada;
	}


	public void setIdTipoEntrada(int idTipoEntrada) {
		this.idTipoEntrada = idTipoEntrada;
	}


	public String getDocumentoAsistente() {
		return documentoAsistente;
	}


	public void setDocumentoAsistente(String documentoAsistente) {
		this.documentoAsistente = documentoAsistente;
	}


	public int getIdDispositivo() {
		return idDispositivo;
	}


	public void setIdDispositivo(int idDispositivo) {
		this.idDispositivo = idDispositivo;
	}
	
	
	
	
}
