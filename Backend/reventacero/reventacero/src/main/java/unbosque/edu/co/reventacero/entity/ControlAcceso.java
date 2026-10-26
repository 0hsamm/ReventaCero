package unbosque.edu.co.reventacero.entity;

import java.time.LocalDateTime;

public class ControlAcceso {

	private int idAcceso;
	private LocalDateTime fechaHoraIngreso;
	private String resultado;
	private String codigoBoleta;
	
	
	public ControlAcceso() {
		// TODO Auto-generated constructor stub
	}


	public ControlAcceso(int idAcceso, LocalDateTime fechaHoraIngreso, String resultado, String codigoBoleta) {
		super();
		this.idAcceso = idAcceso;
		this.fechaHoraIngreso = fechaHoraIngreso;
		this.resultado = resultado;
		this.codigoBoleta = codigoBoleta;
	}


	public int getIdAcceso() {
		return idAcceso;
	}


	public void setIdAcceso(int idAcceso) {
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


	public String getCodigoBoleta() {
		return codigoBoleta;
	}


	public void setCodigoBoleta(String codigoBoleta) {
		this.codigoBoleta = codigoBoleta;
	}
	
	
	
	
}
