package co.edu.unbosque.reventacero.service;

import java.util.List;

import org.modelmapper.ModelMapper;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import co.edu.unbosque.reventacero.dto.EscenarioDTO;
import co.edu.unbosque.reventacero.entity.Escenario;
import co.edu.unbosque.reventacero.entity.Festival;
import co.edu.unbosque.reventacero.exception.RecursoNoEncontradoException;
import co.edu.unbosque.reventacero.exception.ReglaNegocioException;
import co.edu.unbosque.reventacero.repository.EscenarioRepository;
import co.edu.unbosque.reventacero.repository.FestivalRepository;
import co.edu.unbosque.reventacero.repository.PresentacionRepository;
import co.edu.unbosque.reventacero.util.Validaciones;

@Service
public class EscenarioService {

	private final EscenarioRepository escenarioRepo;

	private final FestivalRepository festivalRepo;

	private final PresentacionRepository presentacionRepo;

	private final ModelMapper modelMapper;

	public EscenarioService(EscenarioRepository escenarioRepo, FestivalRepository festivalRepo,
			PresentacionRepository presentacionRepo, ModelMapper modelMapper) {
		this.escenarioRepo = escenarioRepo;
		this.festivalRepo = festivalRepo;
		this.presentacionRepo = presentacionRepo;
		this.modelMapper = modelMapper;
	}

	@Transactional
	public EscenarioDTO crear(EscenarioDTO dto) {

		Validaciones.requerirTexto(dto.getNombreEscenario(), "nombreEscenario");
		Validaciones.requerirPositivo(dto.getCapacidad(), "capacidad");
		dto.setNombreEscenario(dto.getNombreEscenario().trim());

		if (!festivalRepo.existsById(dto.getIdFestival())) {
			throw new RecursoNoEncontradoException("El festival " + dto.getIdFestival() + " no existe");
		}

		if (escenarioRepo.existsById(dto.getIdFestival(), dto.getNombreEscenario())) {
			throw new ReglaNegocioException("El festival ya tiene un escenario llamado " + dto.getNombreEscenario());
		}

		Escenario escenario = aEntidad(dto);
		escenarioRepo.save(escenario);

		return aDTO(escenario);
	}

	@Transactional
	public EscenarioDTO actualizar(long idFestival, String nombreEscenario, EscenarioDTO dto) {

		Escenario existente = buscarEntidad(idFestival, nombreEscenario);

		Validaciones.requerirPositivo(dto.getCapacidad(), "capacidad");

		existente.setCapacidad(dto.getCapacidad());
		escenarioRepo.update(existente);

		return aDTO(existente);
	}

	@Transactional
	public void eliminar(long idFestival, String nombreEscenario) {

		buscarEntidad(idFestival, nombreEscenario);

		if (!presentacionRepo.findByEscenario(idFestival, nombreEscenario).isEmpty()) {
			throw new ReglaNegocioException("No se puede eliminar el escenario porque tiene presentaciones programadas");
		}

		try {
			escenarioRepo.deleteById(idFestival, nombreEscenario);
		} catch (DataIntegrityViolationException e) {
			throw new ReglaNegocioException("No se puede eliminar el escenario porque tiene registros asociados");
		}
	}

	public EscenarioDTO buscar(long idFestival, String nombreEscenario) {
		return aDTO(buscarEntidad(idFestival, nombreEscenario));
	}

	public List<EscenarioDTO> listar() {
		return escenarioRepo.findAll().stream().map(this::aDTO).toList();
	}

	public List<EscenarioDTO> listarPorFestival(long idFestival) {
		return escenarioRepo.findByFestivalId(idFestival).stream().map(this::aDTO).toList();
	}

	private Escenario buscarEntidad(long idFestival, String nombreEscenario) {
		return escenarioRepo.findById(idFestival, nombreEscenario).orElseThrow(() -> new RecursoNoEncontradoException(
				"El escenario " + nombreEscenario + " del festival " + idFestival + " no existe"));
	}

	private EscenarioDTO aDTO(Escenario escenario) {
		EscenarioDTO dto = modelMapper.map(escenario, EscenarioDTO.class);
		dto.setIdFestival(escenario.getFestival() != null ? escenario.getFestival().getIdFestival() : 0);
		return dto;
	}

	private Escenario aEntidad(EscenarioDTO dto) {
		Escenario escenario = modelMapper.map(dto, Escenario.class);
		Festival festival = new Festival();
		festival.setIdFestival(dto.getIdFestival());
		escenario.setFestival(festival);
		return escenario;
	}

}
