package unbosque.edu.co.reventacero.entity;

import java.math.BigDecimal;

public class Patrocinio {

	private int idPatrocinio;
	private BigDecimal monto;
	private String tipoAporte;
	private int idFestival;
	private int idPatrocinador;
	
	
	public Patrocinio() {
		// TODO Auto-generated constructor stub
	}


	public Patrocinio(int idPatrocinio, BigDecimal monto, String tipoAporte, int idFestival, int idPatrocinador) {
		super();
		this.idPatrocinio = idPatrocinio;
		this.monto = monto;
		this.tipoAporte = tipoAporte;
		this.idFestival = idFestival;
		this.idPatrocinador = idPatrocinador;
	}


	public int getIdPatrocinio() {
		return idPatrocinio;
	}


	public void setIdPatrocinio(int idPatrocinio) {
		this.idPatrocinio = idPatrocinio;
	}


	public BigDecimal getMonto() {
		return monto;
	}


	public void setMonto(BigDecimal monto) {
		this.monto = monto;
	}


	public String getTipoAporte() {
		return tipoAporte;
	}


	public void setTipoAporte(String tipoAporte) {
		this.tipoAporte = tipoAporte;
	}


	public int getIdFestival() {
		return idFestival;
	}


	public void setIdFestival(int idFestival) {
		this.idFestival = idFestival;
	}


	public int getIdPatrocinador() {
		return idPatrocinador;
	}


	public void setIdPatrocinador(int idPatrocinador) {
		this.idPatrocinador = idPatrocinador;
	}
	
	
}
