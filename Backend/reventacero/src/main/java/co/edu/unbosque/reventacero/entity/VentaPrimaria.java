package co.edu.unbosque.reventacero.entity;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Objects;

import co.edu.unbosque.reventacero.enums.TipoTransaccion;

public class VentaPrimaria extends Transaccion{

	
	private String canalVena;
	
	
	public VentaPrimaria() {
		// TODO Auto-generated constructor stub
	}


	public VentaPrimaria(String canalVena) {
		super();
		this.canalVena = canalVena;
	}


	public VentaPrimaria(long idTransaccion, LocalDateTime fechaHora, BigDecimal precio, BigDecimal comisionOrganizador,
			String metodoPago, Boleta boleta, Dispositivo dispositivo, TipoTransaccion tipoTransaccion,
			String canalVena) {
		super(idTransaccion, fechaHora, precio, comisionOrganizador, metodoPago, boleta, dispositivo, tipoTransaccion);
		this.canalVena = canalVena;
	}


	public VentaPrimaria(long idTransaccion, LocalDateTime fechaHora, BigDecimal precio, BigDecimal comisionOrganizador,
			String metodoPago, Boleta boleta, Dispositivo dispositivo, TipoTransaccion tipoTransaccion) {
		super(idTransaccion, fechaHora, precio, comisionOrganizador, metodoPago, boleta, dispositivo, tipoTransaccion);
		// TODO Auto-generated constructor stub
	}


	public String getCanalVena() {
		return canalVena;
	}


	public void setCanalVena(String canalVena) {
		this.canalVena = canalVena;
	}


	@Override
	public int hashCode() {
		final int prime = 31;
		int result = super.hashCode();
		result = prime * result + Objects.hash(canalVena);
		return result;
	}


	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (!super.equals(obj))
			return false;
		if (getClass() != obj.getClass())
			return false;
		VentaPrimaria other = (VentaPrimaria) obj;
		return Objects.equals(canalVena, other.canalVena);
	}


	@Override
	public String toString() {
		return "VentaPrimaria [canalVena=" + canalVena + "]";
	}
	
	
	
}
