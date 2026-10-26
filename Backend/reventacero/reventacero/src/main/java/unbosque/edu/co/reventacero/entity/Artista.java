package unbosque.edu.co.reventacero.entity;

public class Artista {

	private int idArtista;
	private String nombreArtistico;
	private String pais;
	private int idGenero;
	
	public Artista() {
		// TODO Auto-generated constructor stub
	}

	public Artista(int idArtista, String nombreArtistico, String pais, int idGenero) {
		super();
		this.idArtista = idArtista;
		this.nombreArtistico = nombreArtistico;
		this.pais = pais;
		this.idGenero = idGenero;
	}

	public int getIdArtista() {
		return idArtista;
	}

	public void setIdArtista(int idArtista) {
		this.idArtista = idArtista;
	}

	public String getNombreArtistico() {
		return nombreArtistico;
	}

	public void setNombreArtistico(String nombreArtistico) {
		this.nombreArtistico = nombreArtistico;
	}

	public String getPais() {
		return pais;
	}

	public void setPais(String pais) {
		this.pais = pais;
	}

	public int getIdGenero() {
		return idGenero;
	}

	public void setIdGenero(int idGenero) {
		this.idGenero = idGenero;
	}
	
	
	
	
}
