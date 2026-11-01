package co.edu.unbosque.reventacero.dto;

public class GeneroDTO {

	
	private int idGenero;
	private String nombre;
	
	
	public GeneroDTO() {
		// TODO Auto-generated constructor stub
	}


	public GeneroDTO(int idGenero, String nombre) {
		super();
		this.idGenero = idGenero;
		this.nombre = nombre;
	}


	public int getIdGenero() {
		return idGenero;
	}


	public void setIdGenero(int idGenero) {
		this.idGenero = idGenero;
	}


	public String getNombre() {
		return nombre;
	}


	public void setNombre(String nombre) {
		this.nombre = nombre;
	}
	
	
	
	
}
