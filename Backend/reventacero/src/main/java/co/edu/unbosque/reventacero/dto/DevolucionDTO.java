package co.edu.unbosque.reventacero.dto;

import java.math.BigDecimal;

public class DevolucionDTO extends TransaccionDTO {

	private String motivo;
	private BigDecimal porcentajeReembolso;

	public DevolucionDTO() {
	}

	public String getMotivo() {
		return motivo;
	}

	public void setMotivo(String motivo) {
		this.motivo = motivo;
	}

	public BigDecimal getPorcentajeReembolso() {
		return porcentajeReembolso;
	}

	public void setPorcentajeReembolso(BigDecimal porcentajeReembolso) {
		this.porcentajeReembolso = porcentajeReembolso;
	}
}