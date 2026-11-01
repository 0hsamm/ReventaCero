package unbosque.edu.co.reventacero.entity;

import java.math.BigDecimal;
import java.util.Objects;

public class Patrocinio {

	
	private long idPatrocinio;
	private BigDecimal monto;
	private String tipoAporte;
	private Patrocinador patrocinador;
	private Festival festival;
	
	public Patrocinio() {
		// TODO Auto-generated constructor stub
	}

	public Patrocinio(long idPatrocinio, BigDecimal monto, String tipoAporte, Patrocinador patrocinador,
			Festival festival) {
		super();
		this.idPatrocinio = idPatrocinio;
		this.monto = monto;
		this.tipoAporte = tipoAporte;
		this.patrocinador = patrocinador;
		this.festival = festival;
	}

	public long getIdPatrocinio() {
		return idPatrocinio;
	}

	public void setIdPatrocinio(long idPatrocinio) {
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

	public Patrocinador getPatrocinador() {
		return patrocinador;
	}

	public void setPatrocinador(Patrocinador patrocinador) {
		this.patrocinador = patrocinador;
	}

	public Festival getFestival() {
		return festival;
	}

	public void setFestival(Festival festival) {
		this.festival = festival;
	}

	@Override
	public int hashCode() {
		return Objects.hash(festival, idPatrocinio, monto, patrocinador, tipoAporte);
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		Patrocinio other = (Patrocinio) obj;
		return Objects.equals(festival, other.festival) && idPatrocinio == other.idPatrocinio
				&& Objects.equals(monto, other.monto) && Objects.equals(patrocinador, other.patrocinador)
				&& Objects.equals(tipoAporte, other.tipoAporte);
	}

	@Override
	public String toString() {
		return "Patrocinio [idPatrocinio=" + idPatrocinio + ", monto=" + monto + ", tipoAporte=" + tipoAporte
				+ ", patrocinador=" + patrocinador + ", festival=" + festival + "]";
	}
	
	
	
	
	
}
