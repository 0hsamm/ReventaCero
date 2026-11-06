package co.edu.unbosque.reventacero.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.modelmapper.ModelMapper;

import co.edu.unbosque.reventacero.dto.ZonaDTO;
import co.edu.unbosque.reventacero.entity.Festival;
import co.edu.unbosque.reventacero.entity.Inscripcion;
import co.edu.unbosque.reventacero.entity.ListaEspera;
import co.edu.unbosque.reventacero.entity.Notificacion;
import co.edu.unbosque.reventacero.entity.TipoEntrada;
import co.edu.unbosque.reventacero.entity.Usuario;
import co.edu.unbosque.reventacero.entity.Zona;
import co.edu.unbosque.reventacero.repository.FestivalRepository;
import co.edu.unbosque.reventacero.repository.InscripcionRepository;
import co.edu.unbosque.reventacero.repository.ListaEsperaRepository;
import co.edu.unbosque.reventacero.repository.NotificacionRepository;
import co.edu.unbosque.reventacero.repository.TipoEntradaRepository;
import co.edu.unbosque.reventacero.repository.UsuarioRepository;
import co.edu.unbosque.reventacero.repository.ZonaRepository;

/** Al subir el aforo de una zona se avisa a tantas personas en espera como cupos nuevos haya. */
class CuposNuevosTest {

	private final NotificacionRepository notificacionRepo = mock(NotificacionRepository.class);
	private final InscripcionRepository inscripcionRepo = mock(InscripcionRepository.class);
	private final TipoEntradaRepository tipoEntradaRepo = mock(TipoEntradaRepository.class);
	private final UsuarioRepository usuarioRepo = mock(UsuarioRepository.class);
	private final BoletaService boletaService = mock(BoletaService.class);
	private final NotificacionService notificaciones = new NotificacionService(notificacionRepo, inscripcionRepo,
			mock(ListaEsperaRepository.class), tipoEntradaRepo, usuarioRepo, boletaService, new ModelMapper());

	@BeforeEach
	void preparar() {
		var tipoEntrada = new TipoEntrada();
		tipoEntrada.setIdTipoEntrada(5);
		tipoEntrada.setCategoria("VIP");
		when(tipoEntradaRepo.findById(5)).thenReturn(Optional.of(tipoEntrada));
		var festival = new Festival();
		festival.setNombre("La mejor fiesta");
		when(boletaService.festivalDe(any())).thenReturn(festival);
		when(usuarioRepo.findByDocumento(anyLong())).thenReturn(Optional.of(cuenta(UsuarioService.ESTADO_ACTIVA)));
		when(inscripcionRepo.findPendientesPorZona(1))
				.thenReturn(List.of(inscripcion(101), inscripcion(102), inscripcion(103)));
	}

	@Test
	void soloAvisaALasPrimerasNPersonas() {
		assertEquals(2, notificaciones.notificarCuposNuevos(1, "Sofa1", 2));

		verify(notificacionRepo, times(2)).save(any());
		verify(inscripcionRepo).actualizarEstado(101, 9, InscripcionService.NOTIFICADA);
		verify(inscripcionRepo).actualizarEstado(102, 9, InscripcionService.NOTIFICADA);
		verify(inscripcionRepo, never()).actualizarEstado(103, 9, InscripcionService.NOTIFICADA);
	}

	@Test
	void conMasCuposQuePersonasAvisaATodas() {
		assertEquals(3, notificaciones.notificarCuposNuevos(1, "Sofa1", 10));
	}

	@Test
	void elMensajeNombraLaZonaConCuposNuevos() {
		notificaciones.notificarCuposNuevos(1, "Sofa1", 1);

		var captor = ArgumentCaptor.forClass(Notificacion.class);
		verify(notificacionRepo).save(captor.capture());
		assertTrue(captor.getValue().getMensaje().contains("Sofa1 (VIP)"));
		assertTrue(captor.getValue().getMensaje().contains("La mejor fiesta"));
	}

	@Test
	void unaCuentaInactivaNoGastaUnCupo() {
		when(usuarioRepo.findByDocumento(101)).thenReturn(Optional.of(cuenta("SUSPENDIDA")));

		assertEquals(1, notificaciones.notificarCuposNuevos(1, "Sofa1", 1));

		verify(inscripcionRepo, never()).actualizarEstado(101, 9, InscripcionService.NOTIFICADA);
		verify(inscripcionRepo).actualizarEstado(102, 9, InscripcionService.NOTIFICADA);
	}

	@Test
	void subirElAforoAvisaPorLaDiferencia() {
		var avisos = mock(NotificacionService.class);
		actualizarAforo(avisos, 50, 53);
		verify(avisos).notificarCuposNuevos(1, "Sofa1", 3);
	}

	@Test
	void noSubirElAforoNoAvisaANadie() {
		var avisos = mock(NotificacionService.class);
		actualizarAforo(avisos, 50, 50);
		actualizarAforo(avisos, 50, 45);
		verify(avisos, never()).notificarCuposNuevos(anyLong(), any(), anyInt());
	}

	private static void actualizarAforo(NotificacionService avisos, int anterior, int nuevo) {
		var festival = new Festival();
		festival.setIdFestival(7);
		var existente = new Zona();
		existente.setIdZona(1);
		existente.setNombre("Sofa1");
		existente.setAforoMaximo(anterior);
		existente.setFestival(festival);
		var zonaRepo = mock(ZonaRepository.class);
		when(zonaRepo.findById(1)).thenReturn(Optional.of(existente));
		var dto = new ZonaDTO();
		dto.setNombre("Sofa1");
		dto.setAforoMaximo(nuevo);
		dto.setIdFestival(7);

		new ZonaService(zonaRepo, mock(FestivalRepository.class), mock(TipoEntradaRepository.class), avisos,
				new ModelMapper()).actualizar(1, dto);
	}

	private static Usuario cuenta(String estado) {
		var usuario = new Usuario();
		usuario.setEstadoCuenta(estado);
		return usuario;
	}

	private static Inscripcion inscripcion(long documento) {
		var usuario = new Usuario();
		usuario.setDocumento(documento);
		var tipoEntrada = new TipoEntrada();
		tipoEntrada.setIdTipoEntrada(5);
		var lista = new ListaEspera();
		lista.setIdLista(9);
		lista.setTipoEntrada(tipoEntrada);
		var inscripcion = new Inscripcion();
		inscripcion.setUsuario(usuario);
		inscripcion.setListaEspera(lista);
		inscripcion.setEstado(InscripcionService.PENDIENTE);
		return inscripcion;
	}
}
