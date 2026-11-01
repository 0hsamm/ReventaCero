package co.edu.unbosque.reventacero.exception;

public class DocumentoYaExisteException extends RuntimeException {

	public DocumentoYaExisteException() {
		super("El documento ya está registrado");
	}

}
