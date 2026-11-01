package unbosque.edu.co.reventacero.entity;

public class Dispositivo {

	private int idDispositivo;
	private String identificadorUnico;
	private String tipo;
	private boolean activo;
	private String documentoAsistente;
	
	
	public Dispositivo() {
		// TODO Auto-generated constructor stub
	}


	public Dispositivo(int idDispositivo, String identificadorUnico, String tipo, boolean activo,
			String documentoAsistente) {
		super();
		this.idDispositivo = idDispositivo;
		this.identificadorUnico = identificadorUnico;
		this.tipo = tipo;
		this.activo = activo;
		this.documentoAsistente = documentoAsistente;
	}


	public int getIdDispositivo() {
		return idDispositivo;
	}


	public void setIdDispositivo(int idDispositivo) {
		this.idDispositivo = idDispositivo;
	}


	public String getIdentificadorUnico() {
		return identificadorUnico;
	}


	public void setIdentificadorUnico(String identificadorUnico) {
		this.identificadorUnico = identificadorUnico;
	}


	public String getTipo() {
		return tipo;
	}


	public void setTipo(String tipo) {
		this.tipo = tipo;
	}


	public boolean isActivo() {
		return activo;
	}


	public void setActivo(boolean activo) {
		this.activo = activo;
	}


	public String getDocumentoAsistente() {
		return documentoAsistente;
	}


	public void setDocumentoAsistente(String documentoAsistente) {
		this.documentoAsistente = documentoAsistente;
	}
	
	
	
	
	
}
