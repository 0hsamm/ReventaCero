package unbosque.edu.co.reventacero.entity;

import java.time.LocalDateTime;
import java.util.Objects;

public class ControlAcceso {

	
	private long idAcceso;
	private LocalDateTime fechaHoraIngreso;
	private String resultado;
	private Boleta boleta;
	
	
	public ControlAcceso() {
		// TODO Auto-generated constructor stub
	}


	public ControlAcceso(long idAcceso, LocalDateTime fechaHoraIngreso, String resultado, Boleta boleta) {
		super();
		this.idAcceso = idAcceso;
		this.fechaHoraIngreso = fechaHoraIngreso;
		this.resultado = resultado;
		this.boleta = boleta;
	}


	public long getIdAcceso() {
		return idAcceso;
	}


	public void setIdAcceso(long idAcceso) {
		this.idAcceso = idAcceso;
	}


	public LocalDateTime getFechaHoraIngreso() {
		return fechaHoraIngreso;
	}


	public void setFechaHoraIngreso(LocalDateTime fechaHoraIngreso) {
		this.fechaHoraIngreso = fechaHoraIngreso;
	}


	public String getResultado() {
		return resultado;
	}


	public void setResultado(String resultado) {
		this.resultado = resultado;
	}


	public Boleta getBoleta() {
		return boleta;
	}


	public void setBoleta(Boleta boleta) {
		this.boleta = boleta;
	}


	@Override
	public int hashCode() {
		return Objects.hash(boleta, fechaHoraIngreso, idAcceso, resultado);
	}


	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		ControlAcceso other = (ControlAcceso) obj;
		return Objects.equals(boleta, other.boleta) && Objects.equals(fechaHoraIngreso, other.fechaHoraIngreso)
				&& idAcceso == other.idAcceso && Objects.equals(resultado, other.resultado);
	}


	@Override
	public String toString() {
		return "ControlAcceso [idAcceso=" + idAcceso + ", fechaHoraIngreso=" + fechaHoraIngreso + ", resultado="
				+ resultado + ", boleta=" + boleta + "]";
	}
	
	
	
	
}
