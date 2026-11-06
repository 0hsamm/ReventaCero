package co.edu.unbosque.reventacero.service;

import java.math.BigDecimal;
import java.util.List;

import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import co.edu.unbosque.reventacero.dto.PatrocinioDTO;
import co.edu.unbosque.reventacero.entity.Festival;
import co.edu.unbosque.reventacero.entity.Patrocinador;
import co.edu.unbosque.reventacero.entity.Patrocinio;
import co.edu.unbosque.reventacero.exception.RecursoNoEncontradoException;
import co.edu.unbosque.reventacero.repository.FestivalRepository;
import co.edu.unbosque.reventacero.repository.PatrocinadorRepository;
import co.edu.unbosque.reventacero.repository.PatrocinioRepository;
import co.edu.unbosque.reventacero.util.Validaciones;

@Service
public class PatrocinioService {

	private final PatrocinioRepository patrocinioRepo;

	private final PatrocinadorRepository patrocinadorRepo;

	private final FestivalRepository festivalRepo;

	private final ModelMapper modelMapper;

	public PatrocinioService(PatrocinioRepository patrocinioRepo, PatrocinadorRepository patrocinadorRepo,
			FestivalRepository festivalRepo, ModelMapper modelMapper) {
		this.patrocinioRepo = patrocinioRepo;
		this.patrocinadorRepo = patrocinadorRepo;
		this.festivalRepo = festivalRepo;
		this.modelMapper = modelMapper;
	}

	@Transactional
	public PatrocinioDTO crear(PatrocinioDTO dto) {

		validar(dto);

		Patrocinio patrocinio = aEntidad(dto);
		patrocinioRepo.save(patrocinio);

		return aDTO(patrocinio);
	}

	@Transactional
	public PatrocinioDTO actualizar(long id, PatrocinioDTO dto) {

		buscarEntidad(id);
		validar(dto);

		dto.setIdPatrocinio(id);
		Patrocinio patrocinio = aEntidad(dto);
		patrocinioRepo.update(patrocinio);

		return aDTO(patrocinio);
	}

	@Transactional
	public void eliminar(long id) {
		buscarEntidad(id);
		patrocinioRepo.deleteById(id);
	}

	public PatrocinioDTO buscarPorId(long id) {
		return aDTO(buscarEntidad(id));
	}

	public List<PatrocinioDTO> listar() {
		return patrocinioRepo.findAll().stream().map(this::aDTO).toList();
	}

	public List<PatrocinioDTO> listarPorFestival(long idFestival) {
		return patrocinioRepo.findByFestivalId(idFestival).stream().map(this::aDTO).toList();
	}

	public List<PatrocinioDTO> listarPorPatrocinador(long idPatrocinador) {
		return patrocinioRepo.findByPatrocinadorId(idPatrocinador).stream().map(this::aDTO).toList();
	}

	public BigDecimal totalPorFestival(long idFestival) {

		if (!festivalRepo.existsById(idFestival)) {
			throw new RecursoNoEncontradoException("El festival " + idFestival + " no existe");
		}

		return patrocinioRepo.findByFestivalId(idFestival).stream()
				.map(Patrocinio::getMonto)
				.filter(monto -> monto != null)
				.reduce(BigDecimal.ZERO, BigDecimal::add);
	}

	private Patrocinio buscarEntidad(long id) {
		return patrocinioRepo.findById(id)
				.orElseThrow(() -> new RecursoNoEncontradoException("El patrocinio " + id + " no existe"));
	}

	private void validar(PatrocinioDTO dto) {

		Validaciones.requerirNoNulo(dto.getMonto(), "monto");

		if (dto.getMonto().signum() <= 0) {
			throw new IllegalArgumentException("El monto del patrocinio debe ser mayor que cero");
		}

		Validaciones.requerirTexto(dto.getTipoAporte(), "tipoAporte");

		if (!patrocinadorRepo.existsById(dto.getIdPatrocinador())) {
			throw new RecursoNoEncontradoException("El patrocinador " + dto.getIdPatrocinador() + " no existe");
		}

		if (!festivalRepo.existsById(dto.getIdFestival())) {
			throw new RecursoNoEncontradoException("El festival " + dto.getIdFestival() + " no existe");
		}
	}

	private PatrocinioDTO aDTO(Patrocinio patrocinio) {
		PatrocinioDTO dto = modelMapper.map(patrocinio, PatrocinioDTO.class);
		dto.setIdPatrocinador(patrocinio.getPatrocinador() != null ? patrocinio.getPatrocinador().getIdPatrocinador() : 0);
		dto.setIdFestival(patrocinio.getFestival() != null ? patrocinio.getFestival().getIdFestival() : 0);
		return dto;
	}

	private Patrocinio aEntidad(PatrocinioDTO dto) {
		Patrocinio patrocinio = modelMapper.map(dto, Patrocinio.class);

		Patrocinador patrocinador = new Patrocinador();
		patrocinador.setIdPatrocinador(dto.getIdPatrocinador());
		patrocinio.setPatrocinador(patrocinador);

		Festival festival = new Festival();
		festival.setIdFestival(dto.getIdFestival());
		patrocinio.setFestival(festival);

		return patrocinio;
	}

}