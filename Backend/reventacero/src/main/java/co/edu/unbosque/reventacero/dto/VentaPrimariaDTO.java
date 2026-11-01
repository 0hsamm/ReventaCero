package co.edu.unbosque.reventacero.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public class VentaPrimariaDTO extends TransaccionDTO{

	
	public VentaPrimariaDTO() {
		// TODO Auto-generated constructor stub
	}

	public VentaPrimariaDTO(int idTransaccion, LocalDateTime fechaHora, BigDecimal precio, BigDecimal comisionOrganizador,
			String codigoBoleta) {
		super(idTransaccion, fechaHora, precio, comisionOrganizador, codigoBoleta);
	}
	
	
	
}
