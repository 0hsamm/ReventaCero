package co.edu.unbosque.reventacero.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/** Oferta de reventa. Trae los datos de la boleta porque el receptor aún no puede consultarla. */
public class SolicitudReventaDTO {

	private long idSolicitud;
	private LocalDateTime fechaHora;
	private BigDecimal precio;
	private String metodoPago;
	private String estado;
	private LocalDateTime fechaRespuesta;
	private long codigoBoleta;
	private long vendedorDocumento;
	private long compradorDocumento;
	private String nombreVendedor;
	private String nombreFestival;
	private String categoriaEntrada;
	private String nombreZona;

	public SolicitudReventaDTO() {
	}

	public long getIdSolicitud() { return idSolicitud; }
	public void setIdSolicitud(long idSolicitud) { this.idSolicitud = idSolicitud; }
	public LocalDateTime getFechaHora() { return fechaHora; }
	public void setFechaHora(LocalDateTime fechaHora) { this.fechaHora = fechaHora; }
	public BigDecimal getPrecio() { return precio; }
	public void setPrecio(BigDecimal precio) { this.precio = precio; }
	public String getMetodoPago() { return metodoPago; }
	public void setMetodoPago(String metodoPago) { this.metodoPago = metodoPago; }
	public String getEstado() { return estado; }
	public void setEstado(String estado) { this.estado = estado; }
	public LocalDateTime getFechaRespuesta() { return fechaRespuesta; }
	public void setFechaRespuesta(LocalDateTime fechaRespuesta) { this.fechaRespuesta = fechaRespuesta; }
	public long getCodigoBoleta() { return codigoBoleta; }
	public void setCodigoBoleta(long codigoBoleta) { this.codigoBoleta = codigoBoleta; }
	public long getVendedorDocumento() { return vendedorDocumento; }
	public void setVendedorDocumento(long vendedorDocumento) { this.vendedorDocumento = vendedorDocumento; }
	public long getCompradorDocumento() { return compradorDocumento; }
	public void setCompradorDocumento(long compradorDocumento) { this.compradorDocumento = compradorDocumento; }
	public String getNombreVendedor() { return nombreVendedor; }
	public void setNombreVendedor(String nombreVendedor) { this.nombreVendedor = nombreVendedor; }
	public String getNombreFestival() { return nombreFestival; }
	public void setNombreFestival(String nombreFestival) { this.nombreFestival = nombreFestival; }
	public String getCategoriaEntrada() { return categoriaEntrada; }
	public void setCategoriaEntrada(String categoriaEntrada) { this.categoriaEntrada = categoriaEntrada; }
	public String getNombreZona() { return nombreZona; }
	public void setNombreZona(String nombreZona) { this.nombreZona = nombreZona; }
}
