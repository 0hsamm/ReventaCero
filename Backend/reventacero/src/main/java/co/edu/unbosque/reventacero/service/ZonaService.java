package co.edu.unbosque.reventacero.service;

import java.util.List;

import org.modelmapper.ModelMapper;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import co.edu.unbosque.reventacero.dto.ZonaDTO;
import co.edu.unbosque.reventacero.entity.Festival;
import co.edu.unbosque.reventacero.entity.Zona;
import co.edu.unbosque.reventacero.exception.RecursoNoEncontradoException;
import co.edu.unbosque.reventacero.exception.ReglaNegocioException;
import co.edu.unbosque.reventacero.repository.FestivalRepository;
import co.edu.unbosque.reventacero.repository.TipoEntradaRepository;
import co.edu.unbosque.reventacero.repository.ZonaRepository;
import co.edu.unbosque.reventacero.util.Validaciones;

@Service
public class ZonaService {

	private final ZonaRepository zonaRepo;

	private final FestivalRepository festivalRepo;

	private final TipoEntradaRepository tipoEntradaRepo;

	private final NotificacionService notificacionService;

	private final ModelMapper modelMapper;

	public ZonaService(ZonaRepository zonaRepo, FestivalRepository festivalRepo,
			TipoEntradaRepository tipoEntradaRepo, NotificacionService notificacionService, ModelMapper modelMapper) {
		this.zonaRepo = zonaRepo;
		this.festivalRepo = festivalRepo;
		this.tipoEntradaRepo = tipoEntradaRepo;
		this.notificacionService = notificacionService;
		this.modelMapper = modelMapper;
	}

	@Transactional
	public ZonaDTO crear(ZonaDTO dto) {

		validar(dto);

		if (!festivalRepo.existsById(dto.getIdFestival())) {
			throw new RecursoNoEncontradoException("El festival " + dto.getIdFestival() + " no existe");
		}

		Zona zona = aEntidad(dto);
		zonaRepo.save(zona);

		return aDTO(zona);
	}

	@Transactional
	public ZonaDTO actualizar(long id, ZonaDTO dto) {

		Zona existente = buscarEntidad(id);
		validar(dto);

		if (existente.getFestival().getIdFestival() != dto.getIdFestival()) {
			throw new ReglaNegocioException("No se puede cambiar el festival de una zona");
		}

		int emitidas = zonaRepo.countBoletasEmitidas(id);
		if (dto.getAforoMaximo() < emitidas) {
			throw new ReglaNegocioException("El aforo máximo (" + dto.getAforoMaximo()
					+ ") no puede ser menor que las boletas ya emitidas (" + emitidas + ")");
		}

		int cuposNuevos = dto.getAforoMaximo() - existente.getAforoMaximo();

		dto.setIdZona(id);
		Zona zona = aEntidad(dto);
		zonaRepo.update(zona);

		if (cuposNuevos > 0) {
			notificacionService.notificarCuposNuevos(id, zona.getNombre(), cuposNuevos);
		}

		return aDTO(zona);
	}

	@Transactional
	public void eliminar(long id) {

		buscarEntidad(id);

		if (!tipoEntradaRepo.findByZonaId(id).isEmpty()) {
			throw new ReglaNegocioException("No se puede eliminar la zona porque tiene tipos de entrada");
		}

		try {
			zonaRepo.deleteById(id);
		} catch (DataIntegrityViolationException e) {
			throw new ReglaNegocioException("No se puede eliminar la zona porque tiene registros asociados");
		}
	}

	public ZonaDTO buscarPorId(long id) {
		return aDTO(buscarEntidad(id));
	}

	public List<ZonaDTO> listar() {
		return zonaRepo.findAll().stream().map(this::aDTO).toList();
	}

	public List<ZonaDTO> listarPorFestival(long idFestival) {
		return zonaRepo.findByFestivalId(idFestival).stream().map(this::aDTO).toList();
	}

	/** Cupos que quedan en la zona: aforo máximo menos las boletas ya emitidas. */
	public int aforoDisponible(long idZona) {
		Zona zona = buscarEntidad(idZona);
		return zona.getAforoMaximo() - zonaRepo.countBoletasEmitidas(idZona);
	}

	private Zona buscarEntidad(long id) {
		return zonaRepo.findById(id)
				.orElseThrow(() -> new RecursoNoEncontradoException("La zona " + id + " no existe"));
	}

	private void validar(ZonaDTO dto) {
		Validaciones.requerirTexto(dto.getNombre(), "nombre");
		Validaciones.requerirPositivo(dto.getAforoMaximo(), "aforoMaximo");
	}

	private ZonaDTO aDTO(Zona zona) {
		ZonaDTO dto = modelMapper.map(zona, ZonaDTO.class);
		dto.setIdFestival(zona.getFestival() != null ? zona.getFestival().getIdFestival() : 0);
		return dto;
	}

	private Zona aEntidad(ZonaDTO dto) {
		Zona zona = modelMapper.map(dto, Zona.class);
		zona.setNombre(dto.getNombre().trim());
		Festival festival = new Festival();
		festival.setIdFestival(dto.getIdFestival());
		zona.setFestival(festival);
		return zona;
	}

}