package co.edu.unbosque.reventacero.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import co.edu.unbosque.reventacero.dto.DispositivoDTO;
import co.edu.unbosque.reventacero.dto.ReventaAutorizadaDTO;
import co.edu.unbosque.reventacero.dto.SolicitudReventaDTO;
import co.edu.unbosque.reventacero.entity.Boleta;
import co.edu.unbosque.reventacero.entity.Usuario;
import co.edu.unbosque.reventacero.exception.ReglaNegocioException;
import co.edu.unbosque.reventacero.repository.SolicitudReventaRepository;

class SolicitudReventaServiceTest {

	private static final long VENDEDOR = 1001;
	private static final long RECEPTOR = 1002;

	private final SolicitudReventaRepository repo = mock(SolicitudReventaRepository.class);
	private final ReventaAutorizadaService reventaService = mock(ReventaAutorizadaService.class);
	private final BoletaService boletaService = mock(BoletaService.class);
	private final DispositivoService dispositivoService = mock(DispositivoService.class);
	private final SolicitudReventaService service = new SolicitudReventaService(repo, reventaService, boletaService,
			dispositivoService);

	private SolicitudReventaDTO solicitud;

	@BeforeEach
	void preparar() {
		solicitud = new SolicitudReventaDTO();
		solicitud.setIdSolicitud(1);
		solicitud.setCodigoBoleta(17);
		solicitud.setVendedorDocumento(VENDEDOR);
		solicitud.setCompradorDocumento(RECEPTOR);
		solicitud.setPrecio(new BigDecimal("25000"));
		solicitud.setMetodoPago("PSE");
		when(repo.findById(1)).thenReturn(Optional.of(solicitud));
		when(boletaService.bloquearYBuscar(17)).thenReturn(boletaDe(VENDEDOR));
		var dispositivo = new DispositivoDTO();
		dispositivo.setDocumentoUsuario(RECEPTOR);
		when(dispositivoService.buscarPorId(7)).thenReturn(dispositivo);
	}

	@Test
	void ofrecerNoRegistraLaReventaNiMueveLaBoleta() {
		when(repo.save(any())).thenReturn(1L);
		var oferta = new SolicitudReventaDTO();
		oferta.setCodigoBoleta(17);
		oferta.setCompradorDocumento(RECEPTOR);
		oferta.setPrecio(new BigDecimal("25000"));
		oferta.setMetodoPago(" PSE ");

		service.crear(oferta, VENDEDOR);

		verify(reventaService).validarCondiciones(17, RECEPTOR, new BigDecimal("25000"));
		assertEquals(VENDEDOR, oferta.getVendedorDocumento());
		assertEquals("PSE", oferta.getMetodoPago());
		verify(repo).save(oferta);
		verify(reventaService, never()).revender(any());
	}

	@Test
	void unaBoletaConOfertaPendienteNoSePuedeOfrecerDeNuevo() {
		when(repo.existePendiente(17)).thenReturn(true);
		var oferta = new SolicitudReventaDTO();
		oferta.setCodigoBoleta(17);
		oferta.setCompradorDocumento(RECEPTOR);
		oferta.setPrecio(BigDecimal.ONE);
		oferta.setMetodoPago("PSE");

		assertThrows(ReglaNegocioException.class, () -> service.crear(oferta, VENDEDOR));
		verify(repo, never()).save(any());
	}

	@Test
	void aceptarRegistraLaReventaConLosDatosOfrecidos() {
		when(repo.resolverSiPendiente(eq(1L), eq(SolicitudReventaService.ACEPTADA), any())).thenReturn(1);

		service.aceptar(1, 7);

		var captor = ArgumentCaptor.forClass(ReventaAutorizadaDTO.class);
		verify(reventaService).revender(captor.capture());
		assertEquals(17, captor.getValue().getCodigoBoleta());
		assertEquals(RECEPTOR, captor.getValue().getCompradorDocumento());
		assertEquals(new BigDecimal("25000"), captor.getValue().getPrecio());
		assertEquals("PSE", captor.getValue().getMetodoPago());
		assertEquals(7, captor.getValue().getIdDispositivo());
	}

	@Test
	void noSeAceptaDosVecesNiTrasRechazar() {
		when(repo.resolverSiPendiente(anyLong(), any(), any())).thenReturn(0);

		assertThrows(ReglaNegocioException.class, () -> service.aceptar(1, 7));
		verify(reventaService, never()).revender(any());
	}

	@Test
	void noSeAceptaSiLaBoletaYaCambioDeDueno() {
		when(repo.resolverSiPendiente(anyLong(), any(), any())).thenReturn(1);
		when(boletaService.bloquearYBuscar(17)).thenReturn(boletaDe(555));

		assertThrows(ReglaNegocioException.class, () -> service.aceptar(1, 7));
		verify(reventaService, never()).revender(any());
	}

	@Test
	void noSeAceptaConElDispositivoDeOtraCuenta() {
		when(repo.resolverSiPendiente(anyLong(), any(), any())).thenReturn(1);
		var ajeno = new DispositivoDTO();
		ajeno.setDocumentoUsuario(VENDEDOR);
		when(dispositivoService.buscarPorId(8)).thenReturn(ajeno);

		assertThrows(ReglaNegocioException.class, () -> service.aceptar(1, 8));
		verify(reventaService, never()).revender(any());
	}

	@Test
	void rechazarDejaLaBoletaConSuDueno() {
		when(repo.resolverSiPendiente(eq(1L), eq(SolicitudReventaService.RECHAZADA), any())).thenReturn(1);

		service.rechazar(1);

		verify(reventaService, never()).revender(any());
		verify(boletaService, never()).bloquearYBuscar(anyLong());
	}

	@Test
	void noSeRechazaUnaSolicitudYaRespondida() {
		when(repo.resolverSiPendiente(anyLong(), any(), any())).thenReturn(0);

		assertThrows(ReglaNegocioException.class, () -> service.rechazar(1));
	}

	private static Boleta boletaDe(long documento) {
		var dueno = new Usuario();
		dueno.setDocumento(documento);
		var boleta = new Boleta();
		boleta.setCodigoBoleta(17);
		boleta.setComprador(dueno);
		return boleta;
	}
}
