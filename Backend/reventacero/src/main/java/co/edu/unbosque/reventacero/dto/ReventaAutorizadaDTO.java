package co.edu.unbosque.reventacero.dto;

public class ReventaAutorizadaDTO extends TransaccionDTO {

	private long vendedorDocumento;
	private long compradorDocumento;

	public ReventaAutorizadaDTO() {
	}

	public long getVendedorDocumento() {
		return vendedorDocumento;
	}

	public void setVendedorDocumento(long vendedorDocumento) {
		this.vendedorDocumento = vendedorDocumento;
	}

	public long getCompradorDocumento() {
		return compradorDocumento;
	}

	public void setCompradorDocumento(long compradorDocumento) {
		this.compradorDocumento = compradorDocumento;
	}
}