package co.edu.unbosque.reventacero.service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import org.modelmapper.ModelMapper;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import co.edu.unbosque.reventacero.dto.NotificacionDTO;
import co.edu.unbosque.reventacero.entity.Festival;
import co.edu.unbosque.reventacero.entity.Inscripcion;
import co.edu.unbosque.reventacero.entity.ListaEspera;
import co.edu.unbosque.reventacero.entity.Notificacion;
import co.edu.unbosque.reventacero.entity.TipoEntrada;
import co.edu.unbosque.reventacero.exception.RecursoNoEncontradoException;
import co.edu.unbosque.reventacero.repository.InscripcionRepository;
import co.edu.unbosque.reventacero.repository.ListaEsperaRepository;
import co.edu.unbosque.reventacero.repository.NotificacionRepository;
import co.edu.unbosque.reventacero.repository.TipoEntradaRepository;
import co.edu.unbosque.reventacero.repository.UsuarioRepository;

@Service
public class NotificacionService {

	public static final String CANAL_APP = "APP";

	private final NotificacionRepository notificacionRepo;

	private final InscripcionRepository inscripcionRepo;

	private final ListaEsperaRepository listaRepo;

	private final TipoEntradaRepository tipoEntradaRepo;

	private final UsuarioRepository usuarioRepo;

	private final BoletaService boletaService;

	private final ModelMapper modelMapper;

	public NotificacionService(NotificacionRepository notificacionRepo, InscripcionRepository inscripcionRepo,
			ListaEsperaRepository listaRepo, TipoEntradaRepository tipoEntradaRepo, UsuarioRepository usuarioRepo,
			BoletaService boletaService, ModelMapper modelMapper) {
		this.notificacionRepo = notificacionRepo;
		this.inscripcionRepo = inscripcionRepo;
		this.listaRepo = listaRepo;
		this.tipoEntradaRepo = tipoEntradaRepo;
		this.usuarioRepo = usuarioRepo;
		this.boletaService = boletaService;
		this.modelMapper = modelMapper;
	}

	@Transactional
	public boolean notificarCupoLiberado(long idTipoEntrada) {

		ListaEspera lista = listaRepo.findByTipoEntradaId(idTipoEntrada).orElse(null);
		if (lista == null) {
			return false;
		}

		TipoEntrada tipoEntrada = tipoEntradaRepo.findById(idTipoEntrada).orElse(null);
		if (tipoEntrada == null) {
			return false;
		}

		Festival festival = boletaService.festivalDe(tipoEntrada);

		for (Inscripcion inscripcion : inscripcionRepo.findByListaEsperaId(lista.getIdLista())) {
			if (InscripcionService.PENDIENTE.equals(inscripcion.getEstado())
					&& cuentaActiva(inscripcion.getUsuario().getDocumento())) {
				String mensaje = "Se liberó un cupo para " + tipoEntrada.getCategoria() + " del festival "
						+ festival.getNombre() + ". Ya puedes comprar tu boleta.";
				guardar(inscripcion, mensaje);
				inscripcionRepo.actualizarEstado(inscripcion.getUsuario().getDocumento(), lista.getIdLista(),
						InscripcionService.NOTIFICADA);
				return true;
			}
		}

		return false;
	}

	/**
	 * Avisa a las primeras personas en espera de una zona, una por cada cupo nuevo. El aviso no reserva
	 * la boleta: quien compre primero se la lleva.
	 */
	@Transactional
	public int notificarCuposNuevos(long idZona, String nombreZona, int cupos) {

		int avisadas = 0;

		for (Inscripcion inscripcion : inscripcionRepo.findPendientesPorZona(idZona)) {

			if (avisadas == cupos) {
				break;
			}

			if (!cuentaActiva(inscripcion.getUsuario().getDocumento())) {
				continue;
			}

			long idTipoEntrada = inscripcion.getListaEspera().getTipoEntrada().getIdTipoEntrada();
			TipoEntrada tipoEntrada = tipoEntradaRepo.findById(idTipoEntrada).orElse(null);

			if (tipoEntrada == null) {
				continue;
			}

			Festival festival = boletaService.festivalDe(tipoEntrada);
			guardar(inscripcion, "Se liberaron cupos en " + nombreZona + " (" + tipoEntrada.getCategoria()
					+ ") del festival " + festival.getNombre() + ". Ya puedes comprar tu boleta.");
			inscripcionRepo.actualizarEstado(inscripcion.getUsuario().getDocumento(),
					inscripcion.getListaEspera().getIdLista(), InscripcionService.NOTIFICADA);
			avisadas++;
		}

		return avisadas;
	}

	@Scheduled(cron = "0 0 8 * * *")
	@Transactional
	public int enviarRecordatoriosProximidad() {

		LocalDate manana = LocalDate.now().plusDays(1);
		int enviados = 0;

		for (ListaEspera lista : listaRepo.findAll()) {

			TipoEntrada tipoEntrada = tipoEntradaRepo.findById(lista.getTipoEntrada().getIdTipoEntrada()).orElse(null);
			if (tipoEntrada == null) {
				continue;
			}

			Festival festival = boletaService.festivalDe(tipoEntrada);
			if (festival.getFechaInicio() == null || !manana.equals(festival.getFechaInicio())) {
				continue;
			}

			String mensaje = "Recordatorio: el festival " + festival.getNombre() + " comienza mañana, " + manana + ".";

			for (Inscripcion inscripcion : inscripcionRepo.findByListaEsperaId(lista.getIdLista())) {
				if (InscripcionService.CANCELADA.equals(inscripcion.getEstado())) {
					continue;
				}
				long documento = inscripcion.getUsuario().getDocumento();
				boolean yaEnviada = notificacionRepo.findByInscripcion(documento, lista.getIdLista()).stream()
						.anyMatch(n -> mensaje.equals(n.getMensaje()));
				if (!yaEnviada) {
					guardar(inscripcion, mensaje);
					enviados++;
				}
			}
		}

		return enviados;
	}

	public NotificacionDTO buscarPorId(long id) {
		return aDTO(notificacionRepo.findById(id)
				.orElseThrow(() -> new RecursoNoEncontradoException("La notificación " + id + " no existe")));
	}

	public List<NotificacionDTO> listar() {
		return notificacionRepo.findAll().stream().map(this::aDTO).toList();
	}

	public List<NotificacionDTO> listarPorUsuario(long documento) {
		if (!usuarioRepo.existsByDocumento(documento)) {
			throw new RecursoNoEncontradoException("El usuario " + documento + " no existe");
		}
		return notificacionRepo.findByUsuarioDocumento(documento).stream().map(this::aDTO).toList();
	}

	public List<NotificacionDTO> listarPorInscripcion(long documento, long idLista) {
		return notificacionRepo.findByInscripcion(documento, idLista).stream().map(this::aDTO).toList();
	}

	private void guardar(Inscripcion inscripcion, String mensaje) {
		Notificacion notificacion = new Notificacion();
		notificacion.setFechaHora(LocalDateTime.now());
		notificacion.setCanal(CANAL_APP);
		notificacion.setMensaje(mensaje);
		notificacion.setInscripcion(inscripcion);
		notificacionRepo.save(notificacion);
	}

	private boolean cuentaActiva(long documento) {
		return usuarioRepo.findByDocumento(documento)
				.map(u -> UsuarioService.ESTADO_ACTIVA.equalsIgnoreCase(u.getEstadoCuenta())).orElse(false);
	}

	private NotificacionDTO aDTO(Notificacion notificacion) {
		NotificacionDTO dto = modelMapper.map(notificacion, NotificacionDTO.class);
		if (notificacion.getInscripcion() != null) {
			dto.setUsuarioDocumento(notificacion.getInscripcion().getUsuario().getDocumento());
			dto.setIdLista(notificacion.getInscripcion().getListaEspera().getIdLista());
		}
		return dto;
	}

}