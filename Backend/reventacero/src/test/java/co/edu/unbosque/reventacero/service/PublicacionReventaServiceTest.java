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
import co.edu.unbosque.reventacero.repository.SolicitudReventaRepository;

class PublicacionReventaServiceTest {

	private static final long VENDEDOR = 1001;
	private static final long COMPRADOR = 1002;

	private final PublicacionReventaRepository repo = mock(PublicacionReventaRepository.class);
	private final SolicitudReventaRepository solicitudRepo = mock(SolicitudReventaRepository.class);
	private final ReventaAutorizadaService reventaService = mock(ReventaAutorizadaService.class);
	private final BoletaService boletaService = mock(BoletaService.class);
	private final DispositivoService dispositivoService = mock(DispositivoService.class);
	private final PublicacionReventaService service = new PublicacionReventaService(repo, solicitudRepo,
			reventaService, boletaService, dispositivoService);

	private PublicacionReventaDTO publicacion;

	@BeforeEach
	void preparar() {
		publicacion = new PublicacionReventaDTO();
		publicacion.setIdPublicacion(1);
		publicacion.setCodigoBoleta(17);
		publicacion.setVendedorDocumento(VENDEDOR);
		publicacion.setPrecio(new BigDecimal("25000"));
		when(repo.findById(1)).thenReturn(Optional.of(publicacion));
		when(boletaService.bloquearYBuscar(17)).thenReturn(boletaDe(VENDEDOR));
		var dispositivo = new DispositivoDTO();
		dispositivo.setDocumentoUsuario(COMPRADOR);
		when(dispositivoService.buscarPorId(7)).thenReturn(dispositivo);
	}

	@Test
	void publicarNoMueveLaBoleta() {
		when(repo.save(any())).thenReturn(1L);
		var nueva = nueva();

		service.publicar(nueva, VENDEDOR);

		verify(reventaService).validarBoletaRevendible(17, BigDecimal.TEN);
		assertEquals(VENDEDOR, nueva.getVendedorDocumento());
		verify(repo).save(nueva);
		verify(reventaService, never()).revender(any());
	}

	@Test
	void noSePublicaConOfertaPendienteNiDosVeces() {
		when(solicitudRepo.existePendiente(17)).thenReturn(true);
		assertThrows(ReglaNegocioException.class, () -> service.publicar(nueva(), VENDEDOR));

		when(solicitudRepo.existePendiente(17)).thenReturn(false);
		when(repo.existeDisponible(17)).thenReturn(true);
		assertThrows(ReglaNegocioException.class, () -> service.publicar(nueva(), VENDEDOR));

		verify(repo, never()).save(any());
	}

	@Test
	void comprarRegistraLaReventaConElPrecioPublicado() {
		when(repo.venderSiDisponible(eq(1L), eq(COMPRADOR), any())).thenReturn(1);

		service.comprar(1, COMPRADOR, 7, "PSE");

		var captor = ArgumentCaptor.forClass(ReventaAutorizadaDTO.class);
		verify(reventaService).revender(captor.capture());
		assertEquals(17, captor.getValue().getCodigoBoleta());
		assertEquals(COMPRADOR, captor.getValue().getCompradorDocumento());
		assertEquals(new BigDecimal("25000"), captor.getValue().getPrecio());
		assertEquals("PSE", captor.getValue().getMetodoPago());
		assertEquals(7, captor.getValue().getIdDispositivo());
	}

	@Test
	void dosPersonasNoCompranLaMismaBoleta() {
		when(repo.venderSiDisponible(anyLong(), anyLong(), any())).thenReturn(0);

		assertThrows(ReglaNegocioException.class, () -> service.comprar(1, COMPRADOR, 7, "PSE"));
		verify(reventaService, never()).revender(any());
	}

	@Test
	void elVendedorNoCompraSuPropiaBoleta() {
		assertThrows(ReglaNegocioException.class, () -> service.comprar(1, VENDEDOR, 7, "PSE"));
		verify(repo, never()).venderSiDisponible(anyLong(), anyLong(), any());
	}

	@Test
	void noSeCompraSiLaBoletaCambioDeDueno() {
		when(repo.venderSiDisponible(anyLong(), anyLong(), any())).thenReturn(1);
		when(boletaService.bloquearYBuscar(17)).thenReturn(boletaDe(555));

		assertThrows(ReglaNegocioException.class, () -> service.comprar(1, COMPRADOR, 7, "PSE"));
		verify(reventaService, never()).revender(any());
	}

	@Test
	void noSeCompraConElDispositivoDeOtraCuenta() {
		when(repo.venderSiDisponible(anyLong(), anyLong(), any())).thenReturn(1);
		var ajeno = new DispositivoDTO();
		ajeno.setDocumentoUsuario(VENDEDOR);
		when(dispositivoService.buscarPorId(8)).thenReturn(ajeno);

		assertThrows(ReglaNegocioException.class, () -> service.comprar(1, COMPRADOR, 8, "PSE"));
		verify(reventaService, never()).revender(any());
	}

	@Test
	void elListadoNoRevelaElCodigoDeLaBoletaNiElDocumento() {
		when(repo.findDisponiblesParaComprador(COMPRADOR)).thenReturn(List.of(publicacion));

		var disponibles = service.disponibles(COMPRADOR);

		assertEquals(0, disponibles.getFirst().getCodigoBoleta());
		assertEquals(0, disponibles.getFirst().getVendedorDocumento());
	}

	@Test
	void noSeRetiraUnaPublicacionYaCerrada() {
		when(repo.retirarSiDisponible(anyLong(), any())).thenReturn(0);

		assertThrows(ReglaNegocioException.class, () -> service.retirar(1));
	}

	private static PublicacionReventaDTO nueva() {
		var dto = new PublicacionReventaDTO();
		dto.setCodigoBoleta(17);
		dto.setPrecio(BigDecimal.TEN);
		return dto;
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
