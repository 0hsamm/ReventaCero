package co.edu.unbosque.reventacero.exception;

public class UsuarioYaExisteException extends RuntimeException {

	public UsuarioYaExisteException() {
		super("El usuario ya existe");
	}

}
