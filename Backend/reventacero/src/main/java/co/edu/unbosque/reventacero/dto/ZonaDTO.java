package co.edu.unbosque.reventacero.dto;

public class ZonaDTO {

	private long idZona;
	private String nombre;
	private String tipoZona;
	private int aforoMaximo;
	private long idFestival;

	public ZonaDTO() {
	}

	public long getIdZona() { return idZona; }
	public void setIdZona(long idZona) { this.idZona = idZona; }
	public String getNombre() { return nombre; }
	public void setNombre(String nombre) { this.nombre = nombre; }
	public String getTipoZona() { return tipoZona; }
	public void setTipoZona(String tipoZona) { this.tipoZona = tipoZona; }
	public int getAforoMaximo() { return aforoMaximo; }
	public void setAforoMaximo(int aforoMaximo) { this.aforoMaximo = aforoMaximo; }
	public long getIdFestival() { return idFestival; }
	public void setIdFestival(long idFestival) { this.idFestival = idFestival; }

}