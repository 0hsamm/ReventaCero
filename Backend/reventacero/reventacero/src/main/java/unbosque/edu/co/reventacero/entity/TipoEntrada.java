package unbosque.edu.co.reventacero.entity;

import java.math.BigDecimal;

public class TipoEntrada {

	
	private int idTipoEntrada;
	private String categoria;
	private BigDecimal precioBase;
	private BigDecimal precioTopeReventa;
	private int idZona;
	
	
	public TipoEntrada() {
		// TODO Auto-generated constructor stub
	}


	public TipoEntrada(int idTipoEntrada, String categoria, BigDecimal precioBase, BigDecimal precioTopeReventa,
			int idZona) {
		super();
		this.idTipoEntrada = idTipoEntrada;
		this.categoria = categoria;
		this.precioBase = precioBase;
		this.precioTopeReventa = precioTopeReventa;
		this.idZona = idZona;
	}


	public int getIdTipoEntrada() {
		return idTipoEntrada;
	}


	public void setIdTipoEntrada(int idTipoEntrada) {
		this.idTipoEntrada = idTipoEntrada;
	}


	public String getCategoria() {
		return categoria;
	}


	public void setCategoria(String categoria) {
		this.categoria = categoria;
	}


	public BigDecimal getPrecioBase() {
		return precioBase;
	}


	public void setPrecioBase(BigDecimal precioBase) {
		this.precioBase = precioBase;
	}


	public BigDecimal getPrecioTopeReventa() {
		return precioTopeReventa;
	}


	public void setPrecioTopeReventa(BigDecimal precioTopeReventa) {
		this.precioTopeReventa = precioTopeReventa;
	}


	public int getIdZona() {
		return idZona;
	}


	public void setIdZona(int idZona) {
		this.idZona = idZona;
	}
	
	
	
	
}
