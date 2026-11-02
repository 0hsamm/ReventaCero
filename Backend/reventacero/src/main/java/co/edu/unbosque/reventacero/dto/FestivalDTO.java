package co.edu.unbosque.reventacero.dto;

import java.time.LocalDate;

public class FestivalDTO {

	private long idFestival;
	private String nombre;
	private int edicion;
	private LocalDate fechaInicio;
	private LocalDate fechaFin;
	private String ubicacionPais;
	private String ubicacionCiudad;
	private String ubicacionDireccion;
	private long documentoOrganizador;

	public FestivalDTO() {
	}

	public long getIdFestival() { return idFestival; }
	public void setIdFestival(long idFestival) { this.idFestival = idFestival; }
	public String getNombre() { return nombre; }
	public void setNombre(String nombre) { this.nombre = nombre; }
	public int getEdicion() { return edicion; }
	public void setEdicion(int edicion) { this.edicion = edicion; }
	public LocalDate getFechaInicio() { return fechaInicio; }
	public void setFechaInicio(LocalDate fechaInicio) { this.fechaInicio = fechaInicio; }
	public LocalDate getFechaFin() { return fechaFin; }
	public void setFechaFin(LocalDate fechaFin) { this.fechaFin = fechaFin; }
	public String getUbicacionPais() { return ubicacionPais; }
	public void setUbicacionPais(String ubicacionPais) { this.ubicacionPais = ubicacionPais; }
	public String getUbicacionCiudad() { return ubicacionCiudad; }
	public void setUbicacionCiudad(String ubicacionCiudad) { this.ubicacionCiudad = ubicacionCiudad; }
	public String getUbicacionDireccion() { return ubicacionDireccion; }
	public void setUbicacionDireccion(String ubicacionDireccion) { this.ubicacionDireccion = ubicacionDireccion; }
	public long getDocumentoOrganizador() { return documentoOrganizador; }
	public void setDocumentoOrganizador(long documentoOrganizador) { this.documentoOrganizador = documentoOrganizador; }

}