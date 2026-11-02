package co.edu.unbosque.reventacero.entity;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Objects;

import co.edu.unbosque.reventacero.enums.TipoTransaccion;

public class ReventaAutorizada extends Transaccion{

	private Usuario vendedor;
	private Usuario comprador;
	
	public ReventaAutorizada() {
		// TODO Auto-generated constructor stub
	}

	
	
	public ReventaAutorizada(Usuario vendedor, Usuario comprador) {
		super();
		this.vendedor = vendedor;
		this.comprador = comprador;
	}



	public ReventaAutorizada(long idTransaccion, LocalDateTime fechaHora, BigDecimal precio,
			BigDecimal comisionOrganizador, String metodoPago, Boleta boleta, Dispositivo dispositivo,
			TipoTransaccion tipoTransaccion, Usuario vendedor, Usuario comprador) {
		super(idTransaccion, fechaHora, precio, comisionOrganizador, metodoPago, boleta, dispositivo, tipoTransaccion);
		this.vendedor = vendedor;
		this.comprador = comprador;
	}

	public ReventaAutorizada(long idTransaccion, LocalDateTime fechaHora, BigDecimal precio,
			BigDecimal comisionOrganizador, String metodoPago, Boleta boleta, Dispositivo dispositivo,
			TipoTransaccion tipoTransaccion) {
		super(idTransaccion, fechaHora, precio, comisionOrganizador, metodoPago, boleta, dispositivo, tipoTransaccion);
		// TODO Auto-generated constructor stub
	}



	public Usuario getVendedor() {
		return vendedor;
	}



	public void setVendedor(Usuario vendedor) {
		this.vendedor = vendedor;
	}



	public Usuario getComprador() {
		return comprador;
	}



	public void setComprador(Usuario comprador) {
		this.comprador = comprador;
	}



	@Override
	public int hashCode() {
		final int prime = 31;
		int result = super.hashCode();
		result = prime * result + Objects.hash(comprador, vendedor);
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
		ReventaAutorizada other = (ReventaAutorizada) obj;
		return Objects.equals(comprador, other.comprador) && Objects.equals(vendedor, other.vendedor);
	}



	@Override
	public String toString() {
		return "ReventaAutorizada [vendedor=" + vendedor + ", comprador=" + comprador + "]";
	}
	
	
	
	
}
