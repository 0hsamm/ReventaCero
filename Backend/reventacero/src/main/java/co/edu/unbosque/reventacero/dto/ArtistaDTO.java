package co.edu.unbosque.reventacero.dto;

import java.util.ArrayList;
import java.util.List;

public class ArtistaDTO {

	private long idArtista;
	private String nombreArtistico;
	private String pais;
	private long idGenero;
	private List<String> redesSociales = new ArrayList<>();
	private List<Long> idArtistasInvitados = new ArrayList<>();

	public ArtistaDTO() {
	}

	public long getIdArtista() { return idArtista; }
	public void setIdArtista(long idArtista) { this.idArtista = idArtista; }
	public String getNombreArtistico() { return nombreArtistico; }
	public void setNombreArtistico(String nombreArtistico) { this.nombreArtistico = nombreArtistico; }
	public String getPais() { return pais; }
	public void setPais(String pais) { this.pais = pais; }
	public long getIdGenero() { return idGenero; }
	public void setIdGenero(long idGenero) { this.idGenero = idGenero; }
	public List<String> getRedesSociales() { return redesSociales; }
	public void setRedesSociales(List<String> redesSociales) { this.redesSociales = redesSociales; }
	public List<Long> getIdArtistasInvitados() { return idArtistasInvitados; }
	public void setIdArtistasInvitados(List<Long> idArtistasInvitados) { this.idArtistasInvitados = idArtistasInvitados; }

}