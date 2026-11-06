package co.edu.unbosque.reventacero.entity;

import java.math.BigDecimal;
import java.util.Objects;

public class TipoEntrada {

	
	private long idTipoEntrada;
	private String categoria;
	private BigDecimal precioBase;
	private BigDecimal precioTopeReventa;
	private Zona zona;
	
	
	public TipoEntrada() {
		// TODO Auto-generated constructor stub
	}


	public TipoEntrada(long idTipoEntrada, String categoria, BigDecimal precioBase, BigDecimal precioTopeReventa,
			Zona zona) {
		super();
		this.idTipoEntrada = idTipoEntrada;
		this.categoria = categoria;
		this.precioBase = precioBase;
		this.precioTopeReventa = precioTopeReventa;
		this.zona = zona;
	}


	public long getIdTipoEntrada() {
		return idTipoEntrada;
	}


	public void setIdTipoEntrada(long idTipoEntrada) {
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


	public Zona getZona() {
		return zona;
	}


	public void setZona(Zona zona) {
		this.zona = zona;
	}


	@Override
	public int hashCode() {
		return Objects.hash(categoria, idTipoEntrada, precioBase, precioTopeReventa, zona);
	}


	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		TipoEntrada other = (TipoEntrada) obj;
		return Objects.equals(categoria, other.categoria) && idTipoEntrada == other.idTipoEntrada
				&& Objects.equals(precioBase, other.precioBase)
				&& Objects.equals(precioTopeReventa, other.precioTopeReventa) && Objects.equals(zona, other.zona);
	}


	@Override
	public String toString() {
		return "TipoEntrada [idTipoEntrada=" + idTipoEntrada + ", categoria=" + categoria + ", precioBase=" + precioBase
				+ ", precioTopeReventa=" + precioTopeReventa + ", zona=" + zona + "]";
	}
	
	
	
	
}
