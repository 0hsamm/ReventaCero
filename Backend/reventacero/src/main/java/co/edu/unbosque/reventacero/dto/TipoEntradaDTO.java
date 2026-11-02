package co.edu.unbosque.reventacero.dto;

import java.math.BigDecimal;

public class TipoEntradaDTO {

	private long idTipoEntrada;
	private String categoria;
	private BigDecimal precioBase;
	private BigDecimal precioTopeReventa;
	private long idZona;

	public TipoEntradaDTO() {
	}

	public long getIdTipoEntrada() { return idTipoEntrada; }
	public void setIdTipoEntrada(long idTipoEntrada) { this.idTipoEntrada = idTipoEntrada; }
	public String getCategoria() { return categoria; }
	public void setCategoria(String categoria) { this.categoria = categoria; }
	public BigDecimal getPrecioBase() { return precioBase; }
	public void setPrecioBase(BigDecimal precioBase) { this.precioBase = precioBase; }
	public BigDecimal getPrecioTopeReventa() { return precioTopeReventa; }
	public void setPrecioTopeReventa(BigDecimal precioTopeReventa) { this.precioTopeReventa = precioTopeReventa; }
	public long getIdZona() { return idZona; }
	public void setIdZona(long idZona) { this.idZona = idZona; }

}