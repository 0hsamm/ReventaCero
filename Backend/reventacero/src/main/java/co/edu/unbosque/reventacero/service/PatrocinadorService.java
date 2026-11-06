package co.edu.unbosque.reventacero.service;

import java.util.List;

import org.modelmapper.ModelMapper;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import co.edu.unbosque.reventacero.dto.PatrocinadorDTO;
import co.edu.unbosque.reventacero.entity.Patrocinador;
import co.edu.unbosque.reventacero.exception.RecursoNoEncontradoException;
import co.edu.unbosque.reventacero.exception.ReglaNegocioException;
import co.edu.unbosque.reventacero.repository.PatrocinadorRepository;
import co.edu.unbosque.reventacero.repository.PatrocinioRepository;
import co.edu.unbosque.reventacero.util.Validaciones;

@Service
public class PatrocinadorService {

	private final PatrocinadorRepository patrocinadorRepo;

	private final PatrocinioRepository patrocinioRepo;

	private final ModelMapper modelMapper;

	public PatrocinadorService(PatrocinadorRepository patrocinadorRepo, PatrocinioRepository patrocinioRepo,
			ModelMapper modelMapper) {
		this.patrocinadorRepo = patrocinadorRepo;
		this.patrocinioRepo = patrocinioRepo;
		this.modelMapper = modelMapper;
	}

	@Transactional
	public PatrocinadorDTO crear(PatrocinadorDTO dto) {

		validar(dto, 0);

		Patrocinador patrocinador = modelMapper.map(dto, Patrocinador.class);
		patrocinador.setNombre(dto.getNombre().trim());
		patrocinadorRepo.save(patrocinador);

		return modelMapper.map(patrocinador, PatrocinadorDTO.class);
	}

	@Transactional
	public PatrocinadorDTO actualizar(long id, PatrocinadorDTO dto) {

		buscarEntidad(id);
		validar(dto, id);

		dto.setIdPatrocinador(id);
		Patrocinador patrocinador = modelMapper.map(dto, Patrocinador.class);
		patrocinador.setNombre(dto.getNombre().trim());
		patrocinadorRepo.update(patrocinador);

		return modelMapper.map(patrocinador, PatrocinadorDTO.class);
	}

	@Transactional
	public void eliminar(long id) {

		buscarEntidad(id);

		if (!patrocinioRepo.findByPatrocinadorId(id).isEmpty()) {
			throw new ReglaNegocioException("No se puede eliminar el patrocinador porque tiene patrocinios registrados");
		}

		try {
			patrocinadorRepo.deleteById(id);
		} catch (DataIntegrityViolationException e) {
			throw new ReglaNegocioException("No se puede eliminar el patrocinador porque tiene registros asociados");
		}
	}

	public PatrocinadorDTO buscarPorId(long id) {
		return modelMapper.map(buscarEntidad(id), PatrocinadorDTO.class);
	}

	public List<PatrocinadorDTO> listar() {
		return patrocinadorRepo.findAll().stream().map(p -> modelMapper.map(p, PatrocinadorDTO.class)).toList();
	}

	private Patrocinador buscarEntidad(long id) {
		return patrocinadorRepo.findById(id)
				.orElseThrow(() -> new RecursoNoEncontradoException("El patrocinador " + id + " no existe"));
	}

	private void validar(PatrocinadorDTO dto, long idPropio) {

		Validaciones.requerirTexto(dto.getNombre(), "nombre");

		for (Patrocinador existente : patrocinadorRepo.findByNombre(dto.getNombre().trim())) {
			if (existente.getIdPatrocinador() != idPropio) {
				throw new ReglaNegocioException("Ya existe un patrocinador con el nombre " + dto.getNombre().trim());
			}
		}
	}

}