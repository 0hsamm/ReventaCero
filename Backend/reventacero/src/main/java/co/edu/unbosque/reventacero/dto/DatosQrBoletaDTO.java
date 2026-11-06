package co.edu.unbosque.reventacero.dto;

import java.time.LocalDate;

public record DatosQrBoletaDTO(
		long codigoBoleta,
		LocalDate fechaEmision,
		long idTipoEntrada,
		String categoria,
		String zona,
		long idFestival,
		String festival,
		int edicion,
		LocalDate fechaInicio,
		LocalDate fechaFin,
		String ciudad,
		long documentoComprador,
		String comprador,
		String revendedor,
		Long idTransaccion,
		String estado) {
}
