package co.edu.unbosque.reventacero.entity;

import java.time.LocalDate;
import java.util.Objects;

public class Inscripcion {
	
	private Usuario usuario;
	private ListaEspera listaEspera;
	private LocalDate fechaSolicitud;
	private String estado;
	
	public Inscripcion() {
		// TODO Auto-generated constructor stub
	}

	public Inscripcion(Usuario usuario, ListaEspera listaEspera, LocalDate fechaSolicitud, String estado) {
		super();
		this.usuario = usuario;
		this.listaEspera = listaEspera;
		this.fechaSolicitud = fechaSolicitud;
		this.estado = estado;
	}
	
	
	

	public Usuario getUsuario() {
		return usuario;
	}

	public void setUsuario(Usuario usuario) {
		this.usuario = usuario;
	}

	public ListaEspera getListaEspera() {
		return listaEspera;
	}

	public void setListaEspera(ListaEspera listaEspera) {
		this.listaEspera = listaEspera;
	}

	public LocalDate getFechaSolicitud() {
		return fechaSolicitud;
	}

	public void setFechaSolicitud(LocalDate fechaSolicitud) {
		this.fechaSolicitud = fechaSolicitud;
	}

	public String getEstado() {
		return estado;
	}

	public void setEstado(String estado) {
		this.estado = estado;
	}

	@Override
	public int hashCode() {
		return Objects.hash(estado, fechaSolicitud, listaEspera, usuario);
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		Inscripcion other = (Inscripcion) obj;
		return Objects.equals(estado, other.estado) && Objects.equals(fechaSolicitud, other.fechaSolicitud)
				&& Objects.equals(listaEspera, other.listaEspera) && Objects.equals(usuario, other.usuario);
	}

	@Override
	public String toString() {
		return "Inscripcion [usuario=" + usuario + ", listaEspera=" + listaEspera + ", fechaSolicitud=" + fechaSolicitud
				+ ", estado=" + estado + "]";
	}
	
	
	
	
	

}
