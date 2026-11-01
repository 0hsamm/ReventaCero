package unbosque.edu.co.reventacero.entity;

import java.util.Objects;

public class ListaEspera {

	
	private long idLista;
	private TipoEntrada tipoEntrada;
	
	
	public ListaEspera() {
		// TODO Auto-generated constructor stub
	}


	public ListaEspera(long idLista, TipoEntrada tipoEntrada) {
		super();
		this.idLista = idLista;
		this.tipoEntrada = tipoEntrada;
	}


	public long getIdLista() {
		return idLista;
	}


	public void setIdLista(long idLista) {
		this.idLista = idLista;
	}


	public TipoEntrada getTipoEntrada() {
		return tipoEntrada;
	}


	public void setTipoEntrada(TipoEntrada tipoEntrada) {
		this.tipoEntrada = tipoEntrada;
	}


	@Override
	public int hashCode() {
		return Objects.hash(idLista, tipoEntrada);
	}


	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		ListaEspera other = (ListaEspera) obj;
		return idLista == other.idLista && Objects.equals(tipoEntrada, other.tipoEntrada);
	}


	@Override
	public String toString() {
		return "ListaEspera [idLista=" + idLista + ", tipoEntrada=" + tipoEntrada + "]";
	}
	
	
	
	
}
