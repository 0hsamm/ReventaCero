package unbosque.edu.co.reventacero.entity;

public class Patrocinador {

	private int idPatrocinador;
	private String nombre;
	private String tipo;
	private String contacto;
	
	public Patrocinador() {
		// TODO Auto-generated constructor stub
	}

	public Patrocinador(int idPatrocinador, String nombre, String tipo, String contacto) {
		super();
		this.idPatrocinador = idPatrocinador;
		this.nombre = nombre;
		this.tipo = tipo;
		this.contacto = contacto;
	}

	public int getIdPatrocinador() {
		return idPatrocinador;
	}

	public void setIdPatrocinador(int idPatrocinador) {
		this.idPatrocinador = idPatrocinador;
	}

	public String getNombre() {
		return nombre;
	}

	public void setNombre(String nombre) {
		this.nombre = nombre;
	}

	public String getTipo() {
		return tipo;
	}

	public void setTipo(String tipo) {
		this.tipo = tipo;
	}

	public String getContacto() {
		return contacto;
	}

	public void setContacto(String contacto) {
		this.contacto = contacto;
	}
	
	
	
	
}
