package unbosque.edu.co.reventacero.entity;

import java.time.LocalDate;
import java.util.Objects;

public class Boleta {

	
	private long codigoBoleta;
	private LocalDate fechaEmision;
	private TipoEntrada tipoEntrada;
	private Usuario comprador;
	
	public Boleta() {
		// TODO Auto-generated constructor stub
	}

	public Boleta(long codigoBoleta, LocalDate fechaEmision, TipoEntrada tipoEntrada, Usuario comprador) {
		super();
		this.codigoBoleta = codigoBoleta;
		this.fechaEmision = fechaEmision;
		this.tipoEntrada = tipoEntrada;
		this.comprador = comprador;
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

	public TipoEntrada getTipoEntrada() {
		return tipoEntrada;
	}

	public void setTipoEntrada(TipoEntrada tipoEntrada) {
		this.tipoEntrada = tipoEntrada;
	}

	public Usuario getComprador() {
		return comprador;
	}

	public void setComprador(Usuario comprador) {
		this.comprador = comprador;
	}

	@Override
	public int hashCode() {
		return Objects.hash(codigoBoleta, comprador, fechaEmision, tipoEntrada);
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		Boleta other = (Boleta) obj;
		return codigoBoleta == other.codigoBoleta && Objects.equals(comprador, other.comprador)
				&& Objects.equals(fechaEmision, other.fechaEmision) && Objects.equals(tipoEntrada, other.tipoEntrada);
	}

	@Override
	public String toString() {
		return "Boleta [codigoBoleta=" + codigoBoleta + ", fechaEmision=" + fechaEmision + ", tipoEntrada="
				+ tipoEntrada + ", comprador=" + comprador + "]";
	}
	
	
	
	
	
}
