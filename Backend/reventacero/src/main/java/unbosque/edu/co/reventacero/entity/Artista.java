package unbosque.edu.co.reventacero.entity;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class Artista {
	
	
	private long idArtista;
	private String nombreArtistico;
	private String pais;
	private Genero genero;
	private List<String> redesSociales = new ArrayList<>();
	private List<Artista> artistaInvitados = new ArrayList<>();
	
	
	public Artista() {
		// TODO Auto-generated constructor stub
	}


	public Artista(long idArtista, String nombreArtistico, String pais, Genero genero, List<String> redesSociales,
			List<Artista> artistaInvitados) {
		super();
		this.idArtista = idArtista;
		this.nombreArtistico = nombreArtistico;
		this.pais = pais;
		this.genero = genero;
		this.redesSociales = redesSociales;
		this.artistaInvitados = artistaInvitados;
	}


	public long getIdArtista() {
		return idArtista;
	}


	public void setIdArtista(long idArtista) {
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


	public Genero getGenero() {
		return genero;
	}


	public void setGenero(Genero genero) {
		this.genero = genero;
	}


	public List<String> getRedesSociales() {
		return redesSociales;
	}


	public void setRedesSociales(List<String> redesSociales) {
		this.redesSociales = redesSociales;
	}


	public List<Artista> getArtistaInvitados() {
		return artistaInvitados;
	}


	public void setArtistaInvitados(List<Artista> artistaInvitados) {
		this.artistaInvitados = artistaInvitados;
	}


	@Override
	public int hashCode() {
		return Objects.hash(artistaInvitados, genero, idArtista, nombreArtistico, pais, redesSociales);
	}


	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		Artista other = (Artista) obj;
		return Objects.equals(artistaInvitados, other.artistaInvitados) && Objects.equals(genero, other.genero)
				&& Objects.equals(idArtista, other.idArtista) && Objects.equals(nombreArtistico, other.nombreArtistico)
				&& Objects.equals(pais, other.pais) && Objects.equals(redesSociales, other.redesSociales);
	}


	@Override
	public String toString() {
		return "Artista [idArtista=" + idArtista + ", nombreArtistico=" + nombreArtistico + ", pais=" + pais
				+ ", genero=" + genero + ", redesSociales=" + redesSociales + ", artistaInvitados=" + artistaInvitados
				+ "]";
	}
	
	

}
