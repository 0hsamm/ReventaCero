package unbosque.edu.co.reventacero.entity;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public class Devolucion extends Transaccion{

	public Devolucion() {
		// TODO Auto-generated constructor stub
	}

	public Devolucion(int idTransaccion, LocalDateTime fechaHora, BigDecimal precio, BigDecimal comisionOrganizador,
			String codigoBoleta) {
		super(idTransaccion, fechaHora, precio, comisionOrganizador, codigoBoleta);
	}
	
	
	
}
