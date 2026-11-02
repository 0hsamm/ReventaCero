package co.edu.unbosque.reventacero.dto;

public class PatrocinadorDTO {

	private long idPatrocinador;
	private String nombre;
	private String tipo;
	private String contacto;

	public PatrocinadorDTO() {
	}

	public long getIdPatrocinador() { return idPatrocinador; }
	public void setIdPatrocinador(long idPatrocinador) { this.idPatrocinador = idPatrocinador; }
	public String getNombre() { return nombre; }
	public void setNombre(String nombre) { this.nombre = nombre; }
	public String getTipo() { return tipo; }
	public void setTipo(String tipo) { this.tipo = tipo; }
	public String getContacto() { return contacto; }
	public void setContacto(String contacto) { this.contacto = contacto; }

}