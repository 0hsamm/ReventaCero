package co.edu.unbosque.reventacero.entity;

import java.util.Objects;

public class Dispositivo {

	
	private long idDispositivo;
	private String identificadorUnico;
	private String tipo;
	private boolean activo;
	private Usuario usuario;
	
	
	public Dispositivo() {
		// TODO Auto-generated constructor stub
	}


	public Dispositivo(long idDispositivo, String identificadorUnico, String tipo, boolean activo, Usuario usuario) {
		super();
		this.idDispositivo = idDispositivo;
		this.identificadorUnico = identificadorUnico;
		this.tipo = tipo;
		this.activo = activo;
		this.usuario = usuario;
	}


	public long getIdDispositivo() {
		return idDispositivo;
	}


	public void setIdDispositivo(long idDispositivo) {
		this.idDispositivo = idDispositivo;
	}


	public String getIdentificadorUnico() {
		return identificadorUnico;
	}


	public void setIdentificadorUnico(String identificadorUnico) {
		this.identificadorUnico = identificadorUnico;
	}


	public String getTipo() {
		return tipo;
	}


	public void setTipo(String tipo) {
		this.tipo = tipo;
	}


	public boolean isActivo() {
		return activo;
	}


	public void setActivo(boolean activo) {
		this.activo = activo;
	}


	public Usuario getUsuario() {
		return usuario;
	}


	public void setUsuario(Usuario usuario) {
		this.usuario = usuario;
	}


	@Override
	public int hashCode() {
		return Objects.hash(activo, idDispositivo, identificadorUnico, tipo, usuario);
	}


	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		Dispositivo other = (Dispositivo) obj;
		return activo == other.activo && idDispositivo == other.idDispositivo
				&& Objects.equals(identificadorUnico, other.identificadorUnico) && Objects.equals(tipo, other.tipo)
				&& Objects.equals(usuario, other.usuario);
	}


	@Override
	public String toString() {
		return "Dispositivo [idDispositivo=" + idDispositivo + ", identificadorUnico=" + identificadorUnico + ", tipo="
				+ tipo + ", activo=" + activo + ", usuario=" + usuario + "]";
	}
	
	
	
	
}
