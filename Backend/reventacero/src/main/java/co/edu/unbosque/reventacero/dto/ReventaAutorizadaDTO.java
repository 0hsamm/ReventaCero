package co.edu.unbosque.reventacero.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public class ReventaAutorizadaDTO extends TransaccionDTO{

	private String documentoVendedor;
	private String documentoComprador;
	
	public ReventaAutorizadaDTO() {
		// TODO Auto-generated constructor stub
	}

	public ReventaAutorizadaDTO(int idTransaccion, LocalDateTime fechaHora, BigDecimal precio,
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
