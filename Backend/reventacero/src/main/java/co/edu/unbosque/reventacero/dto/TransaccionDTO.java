package co.edu.unbosque.reventacero.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public class TransaccionDTO {

	private long idTransaccion;
	private LocalDateTime fechaHora;
	private BigDecimal precio;
	private BigDecimal comisionOrganizador;
	private String metodoPago;
	private long codigoBoleta;
	private long idDispositivo;
	private String tipoTransaccion;

	public TransaccionDTO() {
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

	public long getCodigoBoleta() {
		return codigoBoleta;
	}

	public void setCodigoBoleta(long codigoBoleta) {
		this.codigoBoleta = codigoBoleta;
	}

	public long getIdDispositivo() {
		return idDispositivo;
	}

	public void setIdDispositivo(long idDispositivo) {
		this.idDispositivo = idDispositivo;
	}

	public String getTipoTransaccion() {
		return tipoTransaccion;
	}

	public void setTipoTransaccion(String tipoTransaccion) {
		this.tipoTransaccion = tipoTransaccion;
	}
}