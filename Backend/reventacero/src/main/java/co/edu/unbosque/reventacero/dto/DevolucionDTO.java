package co.edu.unbosque.reventacero.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public class DevolucionDTO extends TransaccionDTO{

	public DevolucionDTO() {
		// TODO Auto-generated constructor stub
	}

	public DevolucionDTO(int idTransaccion, LocalDateTime fechaHora, BigDecimal precio, BigDecimal comisionOrganizador,
			String codigoBoleta) {
		super(idTransaccion, fechaHora, precio, comisionOrganizador, codigoBoleta);
	}
	
	
	
}
