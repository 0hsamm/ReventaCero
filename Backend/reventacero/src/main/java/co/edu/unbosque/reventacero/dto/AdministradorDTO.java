package co.edu.unbosque.reventacero.dto;

import java.time.LocalDate;

public class AdministradorDTO extends UsuarioDTO {

	private String nivelAcceso;
	private LocalDate fechaDesignacion;

	public AdministradorDTO() {
	}

	public String getNivelAcceso() {
		return nivelAcceso;
	}

	public void setNivelAcceso(String nivelAcceso) {
		this.nivelAcceso = nivelAcceso;
	}

	public LocalDate getFechaDesignacion() {
		return fechaDesignacion;
	}

	public void setFechaDesignacion(LocalDate fechaDesignacion) {
		this.fechaDesignacion = fechaDesignacion;
	}
}