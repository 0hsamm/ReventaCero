package unbosque.edu.co.reventacero.entity;

import java.time.LocalDate;
import java.util.Objects;

public class Festival {

	private long idFestival;
	private String nombre;
	private int edicion;
	private LocalDate fechaInicio;
	private LocalDate fechaFin;
	private Ubicacion ubicacion;
	private Organizador organizador;
	
	public Festival() {
		// TODO Auto-generated constructor stub
	}

	public Festival(long idFestival, String nombre, int edicion, LocalDate fechaInicio, LocalDate fechaFin,
			Ubicacion ubicacion, Organizador organizador) {
		super();
		this.idFestival = idFestival;
		this.nombre = nombre;
		this.edicion = edicion;
		this.fechaInicio = fechaInicio;
		this.fechaFin = fechaFin;
		this.ubicacion = ubicacion;
		this.organizador = organizador;
	}

	public long getIdFestival() {
		return idFestival;
	}

	public void setIdFestival(long idFestival) {
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

	public Ubicacion getUbicacion() {
		return ubicacion;
	}

	public void setUbicacion(Ubicacion ubicacion) {
		this.ubicacion = ubicacion;
	}

	public Organizador getOrganizador() {
		return organizador;
	}

	public void setOrganizador(Organizador organizador) {
		this.organizador = organizador;
	}

	@Override
	public int hashCode() {
		return Objects.hash(edicion, fechaFin, fechaInicio, idFestival, nombre, organizador, ubicacion);
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		Festival other = (Festival) obj;
		return edicion == other.edicion && Objects.equals(fechaFin, other.fechaFin)
				&& Objects.equals(fechaInicio, other.fechaInicio) && idFestival == other.idFestival
				&& Objects.equals(nombre, other.nombre) && Objects.equals(organizador, other.organizador)
				&& Objects.equals(ubicacion, other.ubicacion);
	}

	@Override
	public String toString() {
		return "Festival [idFestival=" + idFestival + ", nombre=" + nombre + ", edicion=" + edicion + ", fechaInicio="
				+ fechaInicio + ", fechaFin=" + fechaFin + ", ubicacion=" + ubicacion + ", organizador=" + organizador
				+ "]";
	}
	
	
	
	
}
