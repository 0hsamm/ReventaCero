package unbosque.edu.co.reventacero.entity;

public class ListaEspera {

	private int idLista;
	private int idTipoEntrada;
	
	public ListaEspera() {
		// TODO Auto-generated constructor stub
	}

	public ListaEspera(int idLista, int idTipoEntrada) {
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
