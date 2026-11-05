package co.edu.unbosque.reventacero.dto;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

/** Boleta publicada para reventa, con los datos que necesita quien la va a comprar. */
public class PublicacionReventaDTO {

	private long idPublicacion;
	private LocalDateTime fechaPublicacion;
	private BigDecimal precio;
	private String estado;
	private LocalDateTime fechaCierre;
	private long codigoBoleta;
	private long vendedorDocumento;
	private long compradorDocumento;
	private long idFestival;
	private String nombreFestival;
	private String ciudad;
	private LocalDate fechaInicio;
	private LocalDate fechaFin;
	private String categoriaEntrada;
	private String nombreZona;

	public PublicacionReventaDTO() {
	}

	public long getIdPublicacion() { return idPublicacion; }
	public void setIdPublicacion(long idPublicacion) { this.idPublicacion = idPublicacion; }
	public LocalDateTime getFechaPublicacion() { return fechaPublicacion; }
	public void setFechaPublicacion(LocalDateTime fechaPublicacion) { this.fechaPublicacion = fechaPublicacion; }
	public BigDecimal getPrecio() { return precio; }
	public void setPrecio(BigDecimal precio) { this.precio = precio; }
	public String getEstado() { return estado; }
	public void setEstado(String estado) { this.estado = estado; }
	public LocalDateTime getFechaCierre() { return fechaCierre; }
	public void setFechaCierre(LocalDateTime fechaCierre) { this.fechaCierre = fechaCierre; }
	public long getCodigoBoleta() { return codigoBoleta; }
	public void setCodigoBoleta(long codigoBoleta) { this.codigoBoleta = codigoBoleta; }
	public long getVendedorDocumento() { return vendedorDocumento; }
	public void setVendedorDocumento(long vendedorDocumento) { this.vendedorDocumento = vendedorDocumento; }
	public long getCompradorDocumento() { return compradorDocumento; }
	public void setCompradorDocumento(long compradorDocumento) { this.compradorDocumento = compradorDocumento; }
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
