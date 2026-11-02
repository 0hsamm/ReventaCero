package co.edu.unbosque.reventacero.dto;

public class OrganizadorDTO extends UsuarioDTO {

	private String cargo;
	private Long supervisorDocumento;

	public OrganizadorDTO() {
	}

	public String getCargo() {
		return cargo;
	}

	public void setCargo(String cargo) {
		this.cargo = cargo;
	}

	public Long getSupervisorDocumento() {
		return supervisorDocumento;
	}

	public void setSupervisorDocumento(Long supervisorDocumento) {
		this.supervisorDocumento = supervisorDocumento;
	}
}