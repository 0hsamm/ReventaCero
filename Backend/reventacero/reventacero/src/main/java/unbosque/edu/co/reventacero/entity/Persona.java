package unbosque.edu.co.reventacero.entity;

public class Persona {

	private String documento;
	private String nombre;
	private String apellido;
	private String correo;
	
	
	public Persona() {
		// TODO Auto-generated constructor stub
	}


	public Persona(String documento, String nombre, String apellido, String correo) {
		super();
		this.documento = documento;
		this.nombre = nombre;
		this.apellido = apellido;
		this.correo = correo;
	}


	public String getDocumento() {
		return documento;
	}


	public void setDocumento(String documento) {
		this.documento = documento;
	}


	public String getNombre() {
		return nombre;
	}


	public void setNombre(String nombre) {
		this.nombre = nombre;
	}


	public String getApellido() {
		return apellido;
	}


	public void setApellido(String apellido) {
		this.apellido = apellido;
	}


	public String getCorreo() {
		return correo;
	}


	public void setCorreo(String correo) {
		this.correo = correo;
	}
	
	
	
	
	
}
