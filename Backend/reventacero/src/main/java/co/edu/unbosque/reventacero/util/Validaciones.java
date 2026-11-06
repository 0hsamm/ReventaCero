package co.edu.unbosque.reventacero.util;

import java.math.BigDecimal;

public final class Validaciones {

	private Validaciones() {
	}

	public static boolean estaVacio(String valor) {
		return valor == null || valor.isBlank();
	}

	public static void requerirTexto(String valor, String campo) {
		if (estaVacio(valor)) {
			throw new IllegalArgumentException("El campo " + campo + " es obligatorio");
		}
	}

	public static void requerirNoNulo(Object valor, String campo) {
		if (valor == null) {
			throw new IllegalArgumentException("El campo " + campo + " es obligatorio");
		}
	}

	public static void requerirPositivo(long valor, String campo) {
		if (valor <= 0) {
			throw new IllegalArgumentException("El campo " + campo + " debe ser mayor que cero");
		}
	}

	public static void requerirNoNegativo(BigDecimal valor, String campo) {
		if (valor == null || valor.signum() < 0) {
			throw new IllegalArgumentException("El campo " + campo + " es obligatorio y no puede ser negativo");
		}
	}

}