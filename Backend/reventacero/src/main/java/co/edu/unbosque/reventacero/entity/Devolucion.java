package co.edu.unbosque.reventacero.entity;

import java.math.BigDecimal;
import java.util.Objects;

public class Devolucion extends Transaccion{

	private String motivo;
	private BigDecimal porcentajeReembolso;
	
	
	public Devolucion() {
		// TODO Auto-generated constructor stub
	}


	public Devolucion(String motivo, BigDecimal porcentajeReembolso) {
		super();
		this.motivo = motivo;
		this.porcentajeReembolso = porcentajeReembolso;
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


	@Override
	public int hashCode() {
		final int prime = 31;
		int result = super.hashCode();
		result = prime * result + Objects.hash(motivo, porcentajeReembolso);
		return result;
	}


	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (!super.equals(obj))
			return false;
		if (getClass() != obj.getClass())
			return false;
		Devolucion other = (Devolucion) obj;
		return Objects.equals(motivo, other.motivo) && Objects.equals(porcentajeReembolso, other.porcentajeReembolso);
	}


	@Override
	public String toString() {
		return super.toString() + "Devolucion [motivo=" + motivo + ", porcentajeReembolso=" + porcentajeReembolso + "]";
	}
	
	
	
	
	
}
