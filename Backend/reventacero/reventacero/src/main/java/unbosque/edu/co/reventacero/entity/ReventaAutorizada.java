package unbosque.edu.co.reventacero.entity;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public class ReventaAutorizada extends Transaccion{

	private String documentoVendedor;
	private String documentoComprador;
	
	public ReventaAutorizada() {
		// TODO Auto-generated constructor stub
	}

	public ReventaAutorizada(int idTransaccion, LocalDateTime fechaHora, BigDecimal precio,
			BigDecimal comisionOrganizador, String codigoBoleta) {
		super(idTransaccion, fechaHora, precio, comisionOrganizador, codigoBoleta);
		// TODO Auto-generated constructor stub
	}

	public String getDocumentoVendedor() {
		return documentoVendedor;
	}

	public void setDocumentoVendedor(String documentoVendedor) {
		this.documentoVendedor = documentoVendedor;
	}

	public String getDocumentoComprador() {
		return documentoComprador;
	}

	public void setDocumentoComprador(String documentoComprador) {
		this.documentoComprador = documentoComprador;
	}
	
	
	
}
