package co.edu.unbosque.reventacero.service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import co.edu.unbosque.reventacero.dto.PresentacionDTO;
import co.edu.unbosque.reventacero.entity.Artista;
import co.edu.unbosque.reventacero.entity.Escenario;
import co.edu.unbosque.reventacero.entity.Festival;
import co.edu.unbosque.reventacero.entity.Presentacion;
import co.edu.unbosque.reventacero.exception.RecursoNoEncontradoException;
import co.edu.unbosque.reventacero.exception.ReglaNegocioException;
import co.edu.unbosque.reventacero.repository.ArtistaRepository;
import co.edu.unbosque.reventacero.repository.EscenarioRepository;
import co.edu.unbosque.reventacero.repository.FestivalRepository;
import co.edu.unbosque.reventacero.repository.PresentacionRepository;
import co.edu.unbosque.reventacero.util.Validaciones;

@Service
public class PresentacionService {

	private final PresentacionRepository presentacionRepo;

	private final EscenarioRepository escenarioRepo;

	private final ArtistaRepository artistaRepo;

	private final FestivalRepository festivalRepo;

	private final ModelMapper modelMapper;

	public PresentacionService(PresentacionRepository presentacionRepo, EscenarioRepository escenarioRepo,
			ArtistaRepository artistaRepo, FestivalRepository festivalRepo, ModelMapper modelMapper) {
		this.presentacionRepo = presentacionRepo;
		this.escenarioRepo = escenarioRepo;
		this.artistaRepo = artistaRepo;
		this.festivalRepo = festivalRepo;
		this.modelMapper = modelMapper;
	}

	@Transactional
	public PresentacionDTO crear(PresentacionDTO dto) {

		validar(dto, 0);

		Presentacion presentacion = aEntidad(dto);
		presentacionRepo.save(presentacion);

		return aDTO(presentacion);
	}

	@Transactional
	public PresentacionDTO actualizar(long id, PresentacionDTO dto) {

		buscarEntidad(id);
		validar(dto, id);

		dto.setIdPresentacion(id);
		Presentacion presentacion = aEntidad(dto);
		presentacionRepo.update(presentacion);

		return aDTO(presentacion);
	}

	@Transactional
	public void eliminar(long id) {
		buscarEntidad(id);
		presentacionRepo.deleteById(id);
	}

	public PresentacionDTO buscarPorId(long id) {
		return aDTO(buscarEntidad(id));
	}

	public List<PresentacionDTO> listar() {
		return presentacionRepo.findAll().stream().map(this::aDTO).toList();
	}

	public List<PresentacionDTO> listarPorFestival(long idFestival) {
		return presentacionRepo.findByFestivalId(idFestival).stream().map(this::aDTO).toList();
	}

	public List<PresentacionDTO> listarPorEscenario(long idFestival, String nombreEscenario) {
		return presentacionRepo.findByEscenario(idFestival, nombreEscenario).stream().map(this::aDTO).toList();
	}

	public List<PresentacionDTO> listarPorArtista(long idArtista) {
		return presentacionRepo.findByArtistaId(idArtista).stream().map(this::aDTO).toList();
	}

	public List<PresentacionDTO> listarPorFecha(LocalDate fecha) {
		return presentacionRepo.findByFecha(fecha).stream().map(this::aDTO).toList();
	}

	private Presentacion buscarEntidad(long id) {
		return presentacionRepo.findById(id)
				.orElseThrow(() -> new RecursoNoEncontradoException("La presentación " + id + " no existe"));
	}

	private void validar(PresentacionDTO dto, long idPropio) {

		Validaciones.requerirNoNulo(dto.getFecha(), "fecha");
		Validaciones.requerirNoNulo(dto.getHoraInicio(), "horaInicio");
		Validaciones.requerirNoNulo(dto.getHoraFin(), "horaFin");
		Validaciones.requerirTexto(dto.getNombreEscenario(), "nombreEscenario");

		if (!dto.getHoraFin().isAfter(dto.getHoraInicio())) {
			throw new IllegalArgumentException("La hora de fin debe ser posterior a la hora de inicio");
		}

		if (!dto.getHoraInicio().toLocalDate().equals(dto.getFecha())) {
			throw new IllegalArgumentException("La hora de inicio debe corresponder a la fecha de la presentación");
		}

		if (!escenarioRepo.existsById(dto.getIdFestival(), dto.getNombreEscenario())) {
			throw new RecursoNoEncontradoException("El escenario " + dto.getNombreEscenario()
					+ " del festival " + dto.getIdFestival() + " no existe");
		}

		if (!artistaRepo.existsById(dto.getIdArtista())) {
			throw new RecursoNoEncontradoException("El artista " + dto.getIdArtista() + " no existe");
		}

		Festival festival = festivalRepo.findById(dto.getIdFestival())
				.orElseThrow(() -> new RecursoNoEncontradoException("El festival " + dto.getIdFestival() + " no existe"));

		if (dto.getFecha().isBefore(festival.getFechaInicio()) || dto.getFecha().isAfter(festival.getFechaFin())) {
			throw new ReglaNegocioException("La fecha de la presentación está fuera de las fechas del festival");
		}

		for (Presentacion otra : presentacionRepo.findByEscenario(dto.getIdFestival(), dto.getNombreEscenario())) {
			if (otra.getIdPresentacion() != idPropio && seCruzan(otra, dto)) {
				throw new ReglaNegocioException("El escenario ya tiene otra presentación en ese horario");
			}
		}

		for (Presentacion otra : presentacionRepo.findByArtistaId(dto.getIdArtista())) {
			if (otra.getIdPresentacion() != idPropio && seCruzan(otra, dto)) {
				throw new ReglaNegocioException("El artista ya tiene otra presentación en ese horario");
			}
		}
	}

	private static boolean seCruzan(Presentacion existente, PresentacionDTO nueva) {
		LocalDateTime inicio = existente.getHoraInicio();
		LocalDateTime fin = existente.getHoraFin();
		return inicio.isBefore(nueva.getHoraFin()) && nueva.getHoraInicio().isBefore(fin);
	}

	private PresentacionDTO aDTO(Presentacion presentacion) {
		PresentacionDTO dto = modelMapper.map(presentacion, PresentacionDTO.class);

		if (presentacion.getEscenario() != null) {
			dto.setNombreEscenario(presentacion.getEscenario().getNombreEscenario());
			dto.setIdFestival(presentacion.getEscenario().getFestival() != null
					? presentacion.getEscenario().getFestival().getIdFestival() : 0);
		}

		dto.setIdArtista(presentacion.getArtista() != null ? presentacion.getArtista().getIdArtista() : 0);
		return dto;
	}

	private Presentacion aEntidad(PresentacionDTO dto) {
		Presentacion presentacion = modelMapper.map(dto, Presentacion.class);

		Festival festival = new Festival();
		festival.setIdFestival(dto.getIdFestival());
		Escenario escenario = new Escenario();
		escenario.setFestival(festival);
		escenario.setNombreEscenario(dto.getNombreEscenario());
		presentacion.setEscenario(escenario);

		Artista artista = new Artista();
		artista.setIdArtista(dto.getIdArtista());
		presentacion.setArtista(artista);

		return presentacion;
	}

}