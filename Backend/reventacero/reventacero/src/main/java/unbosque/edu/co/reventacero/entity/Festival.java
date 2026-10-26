package unbosque.edu.co.reventacero.entity;

import java.time.LocalDate;

public class Festival {

	private int idFestival;
	private String nombre;
	private int edicion;
	private LocalDate fechaInicio;
	private LocalDate fechaFin;
	private String documentoOrganizador;
	
	
	public Festival() {
		// TODO Auto-generated constructor stub
	}


	public Festival(int idFestival, String nombre, int edicion, LocalDate fechaInicio, LocalDate fechaFin,
			String documentoOrganizador) {
		super();
		this.idFestival = idFestival;
		this.nombre = nombre;
		this.edicion = edicion;
		this.fechaInicio = fechaInicio;
		this.fechaFin = fechaFin;
		this.documentoOrganizador = documentoOrganizador;
	}


	public int getIdFestival() {
		return idFestival;
	}


	public void setIdFestival(int idFestival) {
		this.idFestival = idFestival;
	}


	public String getNombre() {
		return nombre;
	}


	public void setNombre(String nombre) {
		this.nombre = nombre;
	}


	public int getEdicion() {
		return edicion;
	}


	public void setEdicion(int edicion) {
		this.edicion = edicion;
	}


	public LocalDate getFechaInicio() {
		return fechaInicio;
	}


	public void setFechaInicio(LocalDate fechaInicio) {
		this.fechaInicio = fechaInicio;
	}


	public LocalDate getFechaFin() {
		return fechaFin;
	}


	public void setFechaFin(LocalDate fechaFin) {
		this.fechaFin = fechaFin;
	}


	public String getDocumentoOrganizador() {
		return documentoOrganizador;
	}


	public void setDocumentoOrganizador(String documentoOrganizador) {
		this.documentoOrganizador = documentoOrganizador;
	}
	
	
	
	
	
	
}
