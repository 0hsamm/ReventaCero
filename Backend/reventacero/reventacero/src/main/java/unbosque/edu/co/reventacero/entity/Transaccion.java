package unbosque.edu.co.reventacero.entity;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public class Transaccion {

	private int idTransaccion;
	private LocalDateTime fechaHora;
	private BigDecimal precio;
	private BigDecimal comisionOrganizador;
	private String codigoBoleta;
	
	public Transaccion() {
		// TODO Auto-generated constructor stub
	}

	public Transaccion(int idTransaccion, LocalDateTime fechaHora, BigDecimal precio, BigDecimal comisionOrganizador,
			String codigoBoleta) {
		super();
		this.idTransaccion = idTransaccion;
		this.fechaHora = fechaHora;
		this.precio = precio;
		this.comisionOrganizador = comisionOrganizador;
		this.codigoBoleta = codigoBoleta;
	}

	public int getIdTransaccion() {
		return idTransaccion;
	}

	public void setIdTransaccion(int idTransaccion) {
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

	public String getCodigoBoleta() {
		return codigoBoleta;
	}

	public void setCodigoBoleta(String codigoBoleta) {
		this.codigoBoleta = codigoBoleta;
	}
	
	
	
	
	
}
