package co.edu.unbosque.reventacero.exception;

public class CuentaNoVerificadaException extends RuntimeException {

	public CuentaNoVerificadaException() {
		super("Tu cuenta aún no está verificada. Abre el enlace que te enviamos al correo para activarla.");
	}

}
