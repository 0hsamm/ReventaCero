package co.edu.unbosque.reventacero.dto;

public class DispositivoDTO {

	private long idDispositivo;
	private String identificadorUnico;
	private String tipo;
	private boolean activo;
	private long documentoUsuario;

	public DispositivoDTO() {
	}

	public long getIdDispositivo() {
		return idDispositivo;
	}

	public void setIdDispositivo(long idDispositivo) {
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

	public long getDocumentoUsuario() {
		return documentoUsuario;
	}

	public void setDocumentoUsuario(long documentoUsuario) {
		this.documentoUsuario = documentoUsuario;
	}
}