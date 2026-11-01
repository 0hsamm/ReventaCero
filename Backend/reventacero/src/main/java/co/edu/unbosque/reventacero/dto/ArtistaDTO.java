package co.edu.unbosque.reventacero.dto;

public class ArtistaDTO {

	private int idArtista;
	private String nombreArtistico;
	private String pais;
	private int idGenero;
	
	public ArtistaDTO() {
		// TODO Auto-generated constructor stub
	}

	public ArtistaDTO(int idArtista, String nombreArtistico, String pais, int idGenero) {
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
