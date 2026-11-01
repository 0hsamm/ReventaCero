package co.edu.unbosque.reventacero.entity;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Objects;

import co.edu.unbosque.reventacero.enums.TipoTransaccion;

public class Transaccion {

	private long idTransaccion;
	private LocalDateTime fechaHora;
	private BigDecimal precio;
	private BigDecimal comisionOrganizador;
	private String metodoPago;
	private Boleta boleta;
	private Dispositivo dispositivo;
	private TipoTransaccion tipoTransaccion;
	
	
	public Transaccion() {
		// TODO Auto-generated constructor stub
	}


	public Transaccion(long idTransaccion, LocalDateTime fechaHora, BigDecimal precio, BigDecimal comisionOrganizador,
			String metodoPago, Boleta boleta, Dispositivo dispositivo, TipoTransaccion tipoTransaccion) {
		super();
		this.idTransaccion = idTransaccion;
		this.fechaHora = fechaHora;
		this.precio = precio;
		this.comisionOrganizador = comisionOrganizador;
		this.metodoPago = metodoPago;
		this.boleta = boleta;
		this.dispositivo = dispositivo;
		this.tipoTransaccion = tipoTransaccion;
	}


	public long getIdTransaccion() {
		return idTransaccion;
	}


	public void setIdTransaccion(long idTransaccion) {
		this.idTransaccion = idTransaccion;
	}


	public LocalDateTime getFechaHora() {
		return fechaHora;
	}


	public void setFechaHora(LocalDateTime fechaHora) {
		this.fechaHora = fechaHora;
	}


	public BigDecimal getPrecio() {
		return precio;
	}


	public void setPrecio(BigDecimal precio) {
		this.precio = precio;
	}


	public BigDecimal getComisionOrganizador() {
		return comisionOrganizador;
	}


	public void setComisionOrganizador(BigDecimal comisionOrganizador) {
		this.comisionOrganizador = comisionOrganizador;
	}


	public String getMetodoPago() {
		return metodoPago;
	}


	public void setMetodoPago(String metodoPago) {
		this.metodoPago = metodoPago;
	}


	public Boleta getBoleta() {
		return boleta;
	}


	public void setBoleta(Boleta boleta) {
		this.boleta = boleta;
	}


	public Dispositivo getDispositivo() {
		return dispositivo;
	}


	public void setDispositivo(Dispositivo dispositivo) {
		this.dispositivo = dispositivo;
	}


	public TipoTransaccion getTipoTransaccion() {
		return tipoTransaccion;
	}


	public void setTipoTransaccion(TipoTransaccion tipoTransaccion) {
		this.tipoTransaccion = tipoTransaccion;
	}


	@Override
	public int hashCode() {
		return Objects.hash(boleta, comisionOrganizador, dispositivo, fechaHora, idTransaccion, metodoPago, precio,
				tipoTransaccion);
	}


	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		Transaccion other = (Transaccion) obj;
		return Objects.equals(boleta, other.boleta) && Objects.equals(comisionOrganizador, other.comisionOrganizador)
				&& Objects.equals(dispositivo, other.dispositivo) && Objects.equals(fechaHora, other.fechaHora)
				&& idTransaccion == other.idTransaccion && Objects.equals(metodoPago, other.metodoPago)
				&& Objects.equals(precio, other.precio) && tipoTransaccion == other.tipoTransaccion;
	}


	@Override
	public String toString() {
		return "Transaccion [idTransaccion=" + idTransaccion + ", fechaHora=" + fechaHora + ", precio=" + precio
				+ ", comisionOrganizador=" + comisionOrganizador + ", metodoPago=" + metodoPago + ", boleta=" + boleta
				+ ", dispositivo=" + dispositivo + ", tipoTransaccion=" + tipoTransaccion + "]";
	}
	
	
	
	
	
}
