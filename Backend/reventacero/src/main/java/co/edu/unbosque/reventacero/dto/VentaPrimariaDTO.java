package co.edu.unbosque.reventacero.dto;

public class VentaPrimariaDTO extends TransaccionDTO {

	private long idTipoEntrada;
	private String canalVenta;

	public VentaPrimariaDTO() {
	}

	public long getIdTipoEntrada() {
		return idTipoEntrada;
	}

	public void setIdTipoEntrada(long idTipoEntrada) {
		this.idTipoEntrada = idTipoEntrada;
	}

	public String getCanalVenta() {
		return canalVenta;
	}

	public void setCanalVenta(String canalVenta) {
		this.canalVenta = canalVenta;
	}
}