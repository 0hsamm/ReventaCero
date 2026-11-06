package co.edu.unbosque.reventacero.service;

import java.util.List;
import java.util.Optional;

import org.modelmapper.ModelMapper;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import co.edu.unbosque.reventacero.dto.ListaEsperaDTO;
import co.edu.unbosque.reventacero.entity.ListaEspera;
import co.edu.unbosque.reventacero.entity.TipoEntrada;
import co.edu.unbosque.reventacero.exception.RecursoNoEncontradoException;
import co.edu.unbosque.reventacero.exception.ReglaNegocioException;
import co.edu.unbosque.reventacero.repository.InscripcionRepository;
import co.edu.unbosque.reventacero.repository.ListaEsperaRepository;
import co.edu.unbosque.reventacero.repository.TipoEntradaRepository;
import co.edu.unbosque.reventacero.util.Validaciones;

@Service
public class ListaEsperaService {

	private final ListaEsperaRepository listaRepo;

	private final InscripcionRepository inscripcionRepo;

	private final TipoEntradaRepository tipoEntradaRepo;

	private final ModelMapper modelMapper;

	public ListaEsperaService(ListaEsperaRepository listaRepo, InscripcionRepository inscripcionRepo,
			TipoEntradaRepository tipoEntradaRepo, ModelMapper modelMapper) {
		this.listaRepo = listaRepo;
		this.inscripcionRepo = inscripcionRepo;
		this.tipoEntradaRepo = tipoEntradaRepo;
		this.modelMapper = modelMapper;
	}

	@Transactional
	public ListaEsperaDTO crear(long idTipoEntrada) {

		Validaciones.requerirPositivo(idTipoEntrada, "idTipoEntrada");

		if (listaRepo.findByTipoEntradaId(idTipoEntrada).isPresent()) {
			throw new ReglaNegocioException("El tipo de entrada " + idTipoEntrada + " ya tiene una lista de espera");
		}

		return aDTO(crearEntidad(idTipoEntrada));
	}

	@Transactional
	public void eliminar(long idLista) {

		buscarEntidad(idLista);

		if (!inscripcionRepo.findByListaEsperaId(idLista).isEmpty()) {
			throw new ReglaNegocioException("No se puede eliminar la lista de espera porque tiene inscripciones");
		}

		try {
			listaRepo.deleteById(idLista);
		} catch (DataIntegrityViolationException e) {
			throw new ReglaNegocioException("No se puede eliminar la lista de espera porque tiene registros asociados");
		}
	}

	public ListaEsperaDTO buscarPorId(long idLista) {
		return aDTO(buscarEntidad(idLista));
	}

	public ListaEsperaDTO buscarPorTipoEntrada(long idTipoEntrada) {
		return aDTO(listaRepo.findByTipoEntradaId(idTipoEntrada).orElseThrow(() -> new RecursoNoEncontradoException(
				"El tipo de entrada " + idTipoEntrada + " no tiene lista de espera")));
	}

	public List<ListaEsperaDTO> listar() {
		return listaRepo.findAll().stream().map(this::aDTO).toList();
	}

	@Transactional
	public ListaEspera obtenerOCrear(long idTipoEntrada) {

		Optional<ListaEspera> existente = listaRepo.findByTipoEntradaId(idTipoEntrada);
		if (existente.isPresent()) {
			return existente.get();
		}

		try {
			return crearEntidad(idTipoEntrada);
		} catch (DataIntegrityViolationException e) {
			return listaRepo.findByTipoEntradaId(idTipoEntrada).orElseThrow(
					() -> new ReglaNegocioException("No se pudo crear la lista de espera"));
		}
	}

	private ListaEspera crearEntidad(long idTipoEntrada) {

		if (!tipoEntradaRepo.existsById(idTipoEntrada)) {
			throw new RecursoNoEncontradoException("El tipo de entrada " + idTipoEntrada + " no existe");
		}

		TipoEntrada tipoEntrada = new TipoEntrada();
		tipoEntrada.setIdTipoEntrada(idTipoEntrada);

		ListaEspera lista = new ListaEspera();
		lista.setTipoEntrada(tipoEntrada);

		return listaRepo.save(lista);
	}

	private ListaEspera buscarEntidad(long idLista) {
		return listaRepo.findById(idLista)
				.orElseThrow(() -> new RecursoNoEncontradoException("La lista de espera " + idLista + " no existe"));
	}

	private ListaEsperaDTO aDTO(ListaEspera lista) {
		ListaEsperaDTO dto = modelMapper.map(lista, ListaEsperaDTO.class);
		dto.setIdTipoEntrada(lista.getTipoEntrada() != null ? lista.getTipoEntrada().getIdTipoEntrada() : 0);
		return dto;
	}

}