package co.edu.unbosque.reventacero.dto;

public class ListaEsperaDTO {

	private int idLista;
	private int idTipoEntrada;
	
	public ListaEsperaDTO() {
		// TODO Auto-generated constructor stub
	}

	public ListaEsperaDTO(int idLista, int idTipoEntrada) {
		super();
		this.idLista = idLista;
		this.idTipoEntrada = idTipoEntrada;
	}

	public int getIdLista() {
		return idLista;
	}

	public void setIdLista(int idLista) {
		this.idLista = idLista;
	}

	public int getIdTipoEntrada() {
		return idTipoEntrada;
	}

	public void setIdTipoEntrada(int idTipoEntrada) {
		this.idTipoEntrada = idTipoEntrada;
	}
	
	
	
	
}
