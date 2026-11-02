package co.edu.unbosque.reventacero.dto;

import java.time.LocalDateTime;

public class ControlAccesoDTO {

	private long idAcceso;
	private LocalDateTime fechaHoraIngreso;
	private String resultado;
	private long codigoBoleta;
	private String motivo;

	public ControlAccesoDTO() {
	}

	public long getIdAcceso() {
		return idAcceso;
	}

	public void setIdAcceso(long idAcceso) {
		this.idAcceso = idAcceso;
	}

	public LocalDateTime getFechaHoraIngreso() {
		return fechaHoraIngreso;
	}

	public void setFechaHoraIngreso(LocalDateTime fechaHoraIngreso) {
		this.fechaHoraIngreso = fechaHoraIngreso;
	}

	public String getResultado() {
		return resultado;
	}

	public void setResultado(String resultado) {
		this.resultado = resultado;
	}

	public long getCodigoBoleta() {
		return codigoBoleta;
	}

	public void setCodigoBoleta(long codigoBoleta) {
		this.codigoBoleta = codigoBoleta;
	}

	public String getMotivo() {
		return motivo;
	}

	public void setMotivo(String motivo) {
		this.motivo = motivo;
	}
}