package co.edu.unbosque.reventacero.exception;

public class CorreoYaExisteException extends RuntimeException {

	public CorreoYaExisteException() {
		super("El correo ya está registrado");
	}

}
