package co.edu.unbosque.reventacero.dto;

public class GeneroDTO {

	private long idGenero;
	private String nombre;
	private Long idGeneroPrincipal;

	public GeneroDTO() {
	}

	public long getIdGenero() { return idGenero; }
	public void setIdGenero(long idGenero) { this.idGenero = idGenero; }
	public String getNombre() { return nombre; }
	public void setNombre(String nombre) { this.nombre = nombre; }
	public Long getIdGeneroPrincipal() { return idGeneroPrincipal; }
	public void setIdGeneroPrincipal(Long idGeneroPrincipal) { this.idGeneroPrincipal = idGeneroPrincipal; }

}