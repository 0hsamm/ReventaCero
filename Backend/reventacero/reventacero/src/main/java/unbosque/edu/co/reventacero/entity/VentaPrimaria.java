package unbosque.edu.co.reventacero.entity;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public class VentaPrimaria extends Transaccion{

	
	public VentaPrimaria() {
		// TODO Auto-generated constructor stub
	}

	public VentaPrimaria(int idTransaccion, LocalDateTime fechaHora, BigDecimal precio, BigDecimal comisionOrganizador,
			String codigoBoleta) {
		super(idTransaccion, fechaHora, precio, comisionOrganizador, codigoBoleta);
	}
	
	
	
}
