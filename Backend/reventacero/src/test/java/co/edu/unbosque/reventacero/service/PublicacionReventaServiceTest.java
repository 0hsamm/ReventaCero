package co.edu.unbosque.reventacero.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import co.edu.unbosque.reventacero.dto.DispositivoDTO;
import co.edu.unbosque.reventacero.dto.PublicacionReventaDTO;
import co.edu.unbosque.reventacero.dto.ReventaAutorizadaDTO;
import co.edu.unbosque.reventacero.entity.Boleta;
import co.edu.unbosque.reventacero.entity.Usuario;
import co.edu.unbosque.reventacero.exception.ReglaNegocioException;
import co.edu.unbosque.reventacero.repository.PublicacionReventaRepository;

class PublicacionReventaServiceTest {

	private static final long VENDEDOR = 1001;
	private static final long COMPRADOR = 1002;
	private static final long REFERENCIA = 40;

	private final PublicacionReventaRepository repo = mock(PublicacionReventaRepository.class);
	private final ReventaAutorizadaService reventaService = mock(ReventaAutorizadaService.class);
	private final BoletaService boletaService = mock(BoletaService.class);
	private final DispositivoService dispositivoService = mock(DispositivoService.class);
	private final PublicacionReventaService service = new PublicacionReventaService(repo, reventaService,
			boletaService, dispositivoService);

	private PublicacionReventaDTO publicacion;

	@BeforeEach
	void preparar() {
		publicacion = new PublicacionReventaDTO();
		publicacion.setReferencia(REFERENCIA);
		publicacion.setCodigoBoleta(17);
		publicacion.setVendedorDocumento(VENDEDOR);
		publicacion.setPrecio(new BigDecimal("25000"));
		when(repo.findByReferencia(REFERENCIA)).thenReturn(Optional.of(publicacion));
		when(repo.quitarDeVenta(17)).thenReturn(1);
		when(boletaService.bloquearYBuscar(17)).thenReturn(boletaDe(VENDEDOR));
		var dispositivo = new DispositivoDTO();
		dispositivo.setDocumentoUsuario(COMPRADOR);
		when(dispositivoService.buscarPorId(7)).thenReturn(dispositivo);
	}

	@Test
	void publicarMarcaLaBoletaSinMoverla() {
		when(repo.publicar(17, BigDecimal.TEN)).thenReturn(1);

		service.publicar(17, BigDecimal.TEN);

		verify(reventaService).validarBoletaRevendible(17, BigDecimal.TEN);
		verify(repo).publicar(17, BigDecimal.TEN);
		verify(reventaService, never()).revender(any());
	}

	@Test
	void unaBoletaNoSePublicaDosVeces() {
		when(repo.publicar(17, BigDecimal.TEN)).thenReturn(0);

		assertThrows(ReglaNegocioException.class, () -> service.publicar(17, BigDecimal.TEN));
	}

	@Test
	void comprarRegistraLaReventaConElPrecioPublicado() {
		service.comprar(REFERENCIA, COMPRADOR, 7, "PSE");

		verify(repo).quitarDeVenta(17);
		var captor = ArgumentCaptor.forClass(ReventaAutorizadaDTO.class);
		verify(reventaService).revender(captor.capture());
		assertEquals(17, captor.getValue().getCodigoBoleta());
		assertEquals(COMPRADOR, captor.getValue().getCompradorDocumento());
		assertEquals(new BigDecimal("25000"), captor.getValue().getPrecio());
		assertEquals("PSE", captor.getValue().getMetodoPago());
		assertEquals(7, captor.getValue().getIdDispositivo());
	}

	@Test
	void unaReferenciaVencidaNoSirveParaComprar() {
		when(repo.findByReferencia(REFERENCIA)).thenReturn(Optional.empty());

		assertThrows(ReglaNegocioException.class, () -> service.comprar(REFERENCIA, COMPRADOR, 7, "PSE"));
		verify(reventaService, never()).revender(any());
	}

	@Test
	void dosPersonasNoCompranLaMismaBoleta() {
		when(repo.quitarDeVenta(17)).thenReturn(0);

		assertThrows(ReglaNegocioException.class, () -> service.comprar(REFERENCIA, COMPRADOR, 7, "PSE"));
		verify(reventaService, never()).revender(any());
	}

	@Test
	void elVendedorNoCompraSuPropiaBoleta() {
		assertThrows(ReglaNegocioException.class, () -> service.comprar(REFERENCIA, VENDEDOR, 7, "PSE"));
		verify(repo, never()).quitarDeVenta(anyLong());
	}

	@Test
	void noSeCompraSiLaBoletaCambioDeDueno() {
		when(boletaService.bloquearYBuscar(17)).thenReturn(boletaDe(555));

		assertThrows(ReglaNegocioException.class, () -> service.comprar(REFERENCIA, COMPRADOR, 7, "PSE"));
		verify(reventaService, never()).revender(any());
	}

	@Test
	void noSeCompraConElDispositivoDeOtraCuenta() {
		var ajeno = new DispositivoDTO();
		ajeno.setDocumentoUsuario(VENDEDOR);
		when(dispositivoService.buscarPorId(8)).thenReturn(ajeno);

		assertThrows(ReglaNegocioException.class, () -> service.comprar(REFERENCIA, COMPRADOR, 8, "PSE"));
		verify(reventaService, never()).revender(any());
	}

	@Test
	void elListadoNoRevelaElCodigoDeLaBoletaNiElDocumento() {
		when(repo.findDisponiblesParaComprador(COMPRADOR)).thenReturn(new ArrayList<>(List.of(publicacion)));

		var disponibles = service.disponibles(COMPRADOR);

		assertEquals(REFERENCIA, disponibles.getFirst().getReferencia());
		assertEquals(0, disponibles.getFirst().getCodigoBoleta());
		assertEquals(0, disponibles.getFirst().getVendedorDocumento());
	}

	@Test
	void noSeRetiraUnaBoletaQueNoEstaPublicada() {
		when(repo.quitarDeVenta(17)).thenReturn(0);

		assertThrows(ReglaNegocioException.class, () -> service.retirar(17));
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
