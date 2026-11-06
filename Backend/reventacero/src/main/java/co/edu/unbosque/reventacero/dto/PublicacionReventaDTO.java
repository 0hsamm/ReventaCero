package co.edu.unbosque.reventacero.dto;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * Boleta en venta. La referencia es la última transacción de la boleta: identifica la publicación sin
 * revelar el código (que es lo que da acceso al festival) y cambia en cuanto la boleta se vende.
 */
public class PublicacionReventaDTO {

	private long referencia;
	private BigDecimal precio;
	private long codigoBoleta;
	private long vendedorDocumento;
	private long idFestival;
	private String nombreFestival;
	private String ciudad;
	private LocalDate fechaInicio;
	private LocalDate fechaFin;
	private String categoriaEntrada;
	private String nombreZona;

	public PublicacionReventaDTO() {
	}

	public long getReferencia() { return referencia; }
	public void setReferencia(long referencia) { this.referencia = referencia; }
	public BigDecimal getPrecio() { return precio; }
	public void setPrecio(BigDecimal precio) { this.precio = precio; }
	public long getCodigoBoleta() { return codigoBoleta; }
	public void setCodigoBoleta(long codigoBoleta) { this.codigoBoleta = codigoBoleta; }
	public long getVendedorDocumento() { return vendedorDocumento; }
	public void setVendedorDocumento(long vendedorDocumento) { this.vendedorDocumento = vendedorDocumento; }
	public long getIdFestival() { return idFestival; }
	public void setIdFestival(long idFestival) { this.idFestival = idFestival; }
	public String getNombreFestival() { return nombreFestival; }
	public void setNombreFestival(String nombreFestival) { this.nombreFestival = nombreFestival; }
	public String getCiudad() { return ciudad; }
	public void setCiudad(String ciudad) { this.ciudad = ciudad; }
	public LocalDate getFechaInicio() { return fechaInicio; }
	public void setFechaInicio(LocalDate fechaInicio) { this.fechaInicio = fechaInicio; }
	public LocalDate getFechaFin() { return fechaFin; }
	public void setFechaFin(LocalDate fechaFin) { this.fechaFin = fechaFin; }
	public String getCategoriaEntrada() { return categoriaEntrada; }
	public void setCategoriaEntrada(String categoriaEntrada) { this.categoriaEntrada = categoriaEntrada; }
	public String getNombreZona() { return nombreZona; }
	public void setNombreZona(String nombreZona) { this.nombreZona = nombreZona; }
}
