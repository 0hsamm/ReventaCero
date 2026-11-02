package co.edu.unbosque.reventacero.service;

import java.util.List;
import java.util.Optional;

import org.modelmapper.ModelMapper;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import co.edu.unbosque.reventacero.dto.InscripcionDTO;
import co.edu.unbosque.reventacero.entity.Inscripcion;
import co.edu.unbosque.reventacero.entity.ListaEspera;
import co.edu.unbosque.reventacero.entity.TipoEntrada;
import co.edu.unbosque.reventacero.entity.Usuario;
import co.edu.unbosque.reventacero.exception.RecursoNoEncontradoException;
import co.edu.unbosque.reventacero.exception.ReglaNegocioException;
import co.edu.unbosque.reventacero.repository.InscripcionRepository;
import co.edu.unbosque.reventacero.repository.ListaEsperaRepository;
import co.edu.unbosque.reventacero.repository.TipoEntradaRepository;
import co.edu.unbosque.reventacero.repository.UsuarioRepository;
import co.edu.unbosque.reventacero.util.Validaciones;

@Service
public class InscripcionService {

	public static final String PENDIENTE = "PENDIENTE";

	public static final String NOTIFICADA = "NOTIFICADA";

	public static final String CANCELADA = "CANCELADA";

	private final InscripcionRepository inscripcionRepo;

	private final ListaEsperaRepository listaRepo;

	private final TipoEntradaRepository tipoEntradaRepo;

	private final UsuarioRepository usuarioRepo;

	private final ListaEsperaService listaEsperaService;

	private final UsuarioService usuarioService;

	private final BoletaService boletaService;

	private final ZonaService zonaService;

	private final ModelMapper modelMapper;

	public InscripcionService(InscripcionRepository inscripcionRepo, ListaEsperaRepository listaRepo,
			TipoEntradaRepository tipoEntradaRepo, UsuarioRepository usuarioRepo,
			ListaEsperaService listaEsperaService, UsuarioService usuarioService, BoletaService boletaService,
			ZonaService zonaService, ModelMapper modelMapper) {
		this.inscripcionRepo = inscripcionRepo;
		this.listaRepo = listaRepo;
		this.tipoEntradaRepo = tipoEntradaRepo;
		this.usuarioRepo = usuarioRepo;
		this.listaEsperaService = listaEsperaService;
		this.usuarioService = usuarioService;
		this.boletaService = boletaService;
		this.zonaService = zonaService;
		this.modelMapper = modelMapper;
	}

	@Transactional
	public InscripcionDTO inscribir(InscripcionDTO dto) {

		Validaciones.requerirPositivo(dto.getDocumento(), "documento");
		Validaciones.requerirPositivo(dto.getIdTipoEntrada(), "idTipoEntrada");

		usuarioService.validarCuentaActiva(dto.getDocumento());

		TipoEntrada tipoEntrada = tipoEntradaRepo.findById(dto.getIdTipoEntrada()).orElseThrow(
				() -> new RecursoNoEncontradoException("El tipo de entrada " + dto.getIdTipoEntrada() + " no existe"));

		boletaService.validarFestivalNoFinalizado(boletaService.festivalDe(tipoEntrada));

		if (zonaService.aforoDisponible(tipoEntrada.getZona().getIdZona()) > 0) {
			throw new ReglaNegocioException("Todavía hay boletas disponibles; puedes comprarla directamente");
		}

		ListaEspera lista = listaEsperaService.obtenerOCrear(dto.getIdTipoEntrada());

		Optional<Inscripcion> existente = inscripcionRepo.findById(dto.getDocumento(), lista.getIdLista());

		if (existente.isPresent()) {
			if (!CANCELADA.equals(existente.get().getEstado())) {
				throw new ReglaNegocioException("Ya estás inscrito en la lista de espera de este tipo de entrada");
			}
			inscripcionRepo.reactivar(dto.getDocumento(), lista.getIdLista(), PENDIENTE);
		} else {
			Usuario usuario = new Usuario();
			usuario.setDocumento(dto.getDocumento());

			Inscripcion inscripcion = new Inscripcion();
			inscripcion.setUsuario(usuario);
			inscripcion.setListaEspera(lista);
			inscripcion.setEstado(PENDIENTE);

			try {
				inscripcionRepo.save(inscripcion);
			} catch (DataIntegrityViolationException e) {
				throw new ReglaNegocioException("Ya estás inscrito en la lista de espera de este tipo de entrada");
			}
		}

		return aDTO(inscripcionRepo.findById(dto.getDocumento(), lista.getIdLista()).orElseThrow(
				() -> new RecursoNoEncontradoException("No se pudo registrar la inscripción")), tipoEntrada.getIdTipoEntrada());
	}

	@Transactional
	public InscripcionDTO cancelar(long documento, long idLista) {

		Inscripcion inscripcion = buscarEntidad(documento, idLista);

		if (CANCELADA.equals(inscripcion.getEstado())) {
			throw new ReglaNegocioException("La inscripción ya está cancelada");
		}

		inscripcionRepo.actualizarEstado(documento, idLista, CANCELADA);
		inscripcion.setEstado(CANCELADA);

		return aDTO(inscripcion, 0);
	}

	public InscripcionDTO buscar(long documento, long idLista) {
		return aDTO(buscarEntidad(documento, idLista), 0);
	}

	public int posicionEnCola(long documento, long idLista) {

		Inscripcion inscripcion = buscarEntidad(documento, idLista);

		if (!PENDIENTE.equals(inscripcion.getEstado())) {
			throw new ReglaNegocioException("La inscripción no está pendiente");
		}

		int posicion = 1;
		for (Inscripcion otra : inscripcionRepo.findByListaEsperaId(idLista)) {
			if (otra.getUsuario().getDocumento() == documento) {
				return posicion;
			}
			if (PENDIENTE.equals(otra.getEstado())) {
				posicion++;
			}
		}

		return posicion;
	}

	public List<InscripcionDTO> listar() {
		return inscripcionRepo.findAll().stream().map(i -> aDTO(i, 0)).toList();
	}

	public List<InscripcionDTO> listarPorUsuario(long documento) {
		if (!usuarioRepo.existsByDocumento(documento)) {
			throw new RecursoNoEncontradoException("El usuario " + documento + " no existe");
		}
		return inscripcionRepo.findByUsuarioDocumento(documento).stream().map(i -> aDTO(i, 0)).toList();
	}

	public List<InscripcionDTO> listarPorLista(long idLista) {
		if (!listaRepo.existsById(idLista)) {
			throw new RecursoNoEncontradoException("La lista de espera " + idLista + " no existe");
		}
		return inscripcionRepo.findByListaEsperaId(idLista).stream().map(i -> aDTO(i, 0)).toList();
	}

	private Inscripcion buscarEntidad(long documento, long idLista) {
		return inscripcionRepo.findById(documento, idLista).orElseThrow(() -> new RecursoNoEncontradoException(
				"El usuario " + documento + " no está inscrito en la lista " + idLista));
	}

	private InscripcionDTO aDTO(Inscripcion inscripcion, long idTipoEntrada) {
		InscripcionDTO dto = modelMapper.map(inscripcion, InscripcionDTO.class);
		dto.setDocumento(inscripcion.getUsuario() != null ? inscripcion.getUsuario().getDocumento() : 0);
		dto.setIdLista(inscripcion.getListaEspera() != null ? inscripcion.getListaEspera().getIdLista() : 0);
		dto.setIdTipoEntrada(idTipoEntrada > 0 ? idTipoEntrada : tipoEntradaDeLista(dto.getIdLista()));
		return dto;
	}

	private long tipoEntradaDeLista(long idLista) {
		return listaRepo.findById(idLista).map(l -> l.getTipoEntrada().getIdTipoEntrada()).orElse(0L);
	}

}