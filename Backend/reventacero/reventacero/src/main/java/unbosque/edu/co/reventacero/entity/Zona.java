package unbosque.edu.co.reventacero.entity;

public class Zona {

	private int idZona;
	private String nombre;
	private String tipoZona;
	private int aforoMaximo;
	private int idFestival;
	
	
	public Zona() {
		// TODO Auto-generated constructor stub
	}


	public Zona(int idZona, String nombre, String tipoZona, int aforoMaximo, int idFestival) {
		super();
		this.idZona = idZona;
		this.nombre = nombre;
		this.tipoZona = tipoZona;
		this.aforoMaximo = aforoMaximo;
		this.idFestival = idFestival;
	}


	public int getIdZona() {
		return idZona;
	}


	public void setIdZona(int idZona) {
		this.idZona = idZona;
	}


	public String getNombre() {
		return nombre;
	}


	public void setNombre(String nombre) {
		this.nombre = nombre;
	}


	public String getTipoZona() {
		return tipoZona;
	}


	public void setTipoZona(String tipoZona) {
		this.tipoZona = tipoZona;
	}


	public int getAforoMaximo() {
		return aforoMaximo;
	}


	public void setAforoMaximo(int aforoMaximo) {
		this.aforoMaximo = aforoMaximo;
	}


	public int getIdFestival() {
		return idFestival;
	}


	public void setIdFestival(int idFestival) {
		this.idFestival = idFestival;
	}
	
	
	
	
}
