package co.edu.unbosque.reventacero.dto;

import java.math.BigDecimal;

public class PatrocinioDTO {

	private long idPatrocinio;
	private BigDecimal monto;
	private String tipoAporte;
	private long idFestival;
	private long idPatrocinador;

	public PatrocinioDTO() {
	}

	public long getIdPatrocinio() { return idPatrocinio; }
	public void setIdPatrocinio(long idPatrocinio) { this.idPatrocinio = idPatrocinio; }
	public BigDecimal getMonto() { return monto; }
	public void setMonto(BigDecimal monto) { this.monto = monto; }
	public String getTipoAporte() { return tipoAporte; }
	public void setTipoAporte(String tipoAporte) { this.tipoAporte = tipoAporte; }
	public long getIdFestival() { return idFestival; }
	public void setIdFestival(long idFestival) { this.idFestival = idFestival; }
	public long getIdPatrocinador() { return idPatrocinador; }
	public void setIdPatrocinador(long idPatrocinador) { this.idPatrocinador = idPatrocinador; }

}