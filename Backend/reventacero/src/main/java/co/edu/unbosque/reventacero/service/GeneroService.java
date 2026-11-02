package co.edu.unbosque.reventacero.service;

import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import org.modelmapper.ModelMapper;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import co.edu.unbosque.reventacero.dto.GeneroDTO;
import co.edu.unbosque.reventacero.entity.Genero;
import co.edu.unbosque.reventacero.exception.RecursoNoEncontradoException;
import co.edu.unbosque.reventacero.exception.ReglaNegocioException;
import co.edu.unbosque.reventacero.repository.GeneroRepository;
import co.edu.unbosque.reventacero.util.Validaciones;

@Service
public class GeneroService {

	private final GeneroRepository generoRepo;

	private final ModelMapper modelMapper;

	public GeneroService(GeneroRepository generoRepo, ModelMapper modelMapper) {
		this.generoRepo = generoRepo;
		this.modelMapper = modelMapper;
	}

	@Transactional
	public GeneroDTO crear(GeneroDTO dto) {

		Validaciones.requerirTexto(dto.getNombre(), "nombre");
		validarNombreUnico(dto.getNombre().trim(), 0);
		validarPrincipal(0, dto.getIdGeneroPrincipal());

		Genero genero = aEntidad(dto);
		genero.setNombre(dto.getNombre().trim());
		generoRepo.save(genero);

		return aDTO(genero);
	}

	@Transactional
	public GeneroDTO actualizar(long id, GeneroDTO dto) {

		buscarEntidad(id);

		Validaciones.requerirTexto(dto.getNombre(), "nombre");
		validarNombreUnico(dto.getNombre().trim(), id);
		validarPrincipal(id, dto.getIdGeneroPrincipal());

		dto.setIdGenero(id);
		Genero genero = aEntidad(dto);
		genero.setNombre(dto.getNombre().trim());
		generoRepo.update(genero);

		return aDTO(genero);
	}

	@Transactional
	public void eliminar(long id) {

		buscarEntidad(id);

		if (!generoRepo.findByGeneroPrincipalId(id).isEmpty()) {
			throw new ReglaNegocioException("No se puede eliminar el género porque tiene subgéneros");
		}

		try {
			generoRepo.deleteById(id);
		} catch (DataIntegrityViolationException e) {
			throw new ReglaNegocioException("No se puede eliminar el género porque tiene artistas asociados");
		}
	}

	public GeneroDTO buscarPorId(long id) {
		return aDTO(buscarEntidad(id));
	}

	public List<GeneroDTO> listar() {
		return generoRepo.findAll().stream().map(this::aDTO).toList();
	}

	public List<GeneroDTO> listarSubgeneros(long idGeneroPrincipal) {
		buscarEntidad(idGeneroPrincipal);
		return generoRepo.findByGeneroPrincipalId(idGeneroPrincipal).stream().map(this::aDTO).toList();
	}

	private Genero buscarEntidad(long id) {
		return generoRepo.findById(id)
				.orElseThrow(() -> new RecursoNoEncontradoException("El género " + id + " no existe"));
	}

	/** Dos géneros no pueden llamarse igual. idPropio es 0 al crear. */
	private void validarNombreUnico(String nombre, long idPropio) {
		for (Genero existente : generoRepo.findByNombre(nombre)) {
			if (existente.getIdGenero() != idPropio) {
				throw new ReglaNegocioException("Ya existe un género con el nombre " + nombre);
			}
		}
	}

	private void validarPrincipal(long idGenero, Long idPrincipal) {

		if (idPrincipal == null) {
			return;
		}

		if (idPrincipal == idGenero) {
			throw new ReglaNegocioException("Un género no puede ser su propio género principal");
		}

		Set<Long> visitados = new HashSet<>();
		long actual = idPrincipal;

		while (visitados.add(actual)) {
			Optional<Genero> encontrado = generoRepo.findById(actual);

			if (encontrado.isEmpty()) {
				throw new RecursoNoEncontradoException("El género principal " + actual + " no existe");
			}

			Genero padre = encontrado.get().getGeneroPrincipal();

			if (padre == null) {
				return;
			}

			if (padre.getIdGenero() == idGenero) {
				throw new ReglaNegocioException("El género principal genera un ciclo entre géneros");
			}

			actual = padre.getIdGenero();
		}
	}

	private GeneroDTO aDTO(Genero genero) {
		GeneroDTO dto = modelMapper.map(genero, GeneroDTO.class);
		dto.setIdGeneroPrincipal(genero.getGeneroPrincipal() != null ? genero.getGeneroPrincipal().getIdGenero() : null);
		return dto;
	}

	private Genero aEntidad(GeneroDTO dto) {
		Genero genero = modelMapper.map(dto, Genero.class);
		if (dto.getIdGeneroPrincipal() != null) {
			Genero principal = new Genero();
			principal.setIdGenero(dto.getIdGeneroPrincipal());
			genero.setGeneroPrincipal(principal);
		} else {
			genero.setGeneroPrincipal(null);
		}
		return genero;
	}

}