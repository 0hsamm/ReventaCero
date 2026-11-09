package co.edu.unbosque.reventacero.enums;

import org.springframework.security.core.GrantedAuthority;

public enum TipoUsuario implements GrantedAuthority {

	ADMINISTRADOR, ORGANIZADOR, USUARIO;

	/**
	 * Spring Security espera el prefijo ROLE_
	 * (ROLE_ADMINISTRADOR, ROLE_ORGANIZADOR, ROLE_USUARIO).
	 */
	@Override
	public String getAuthority() {
		return "ROLE_" + name();
	}

}
