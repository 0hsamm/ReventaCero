package co.edu.unbosque.reventacero.service;

import java.util.List;

import org.modelmapper.ModelMapper;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import co.edu.unbosque.reventacero.dto.FestivalDTO;
import co.edu.unbosque.reventacero.entity.Festival;
import co.edu.unbosque.reventacero.entity.Organizador;
import co.edu.unbosque.reventacero.entity.Presentacion;
import co.edu.unbosque.reventacero.entity.Ubicacion;
import co.edu.unbosque.reventacero.exception.RecursoNoEncontradoException;
import co.edu.unbosque.reventacero.exception.ReglaNegocioException;
import co.edu.unbosque.reventacero.repository.FestivalRepository;
import co.edu.unbosque.reventacero.repository.OrganizadorRepository;
import co.edu.unbosque.reventacero.repository.PresentacionRepository;
import co.edu.unbosque.reventacero.util.Validaciones;

@Service
public class FestivalService {

	private final FestivalRepository festivalRepo;

	private final OrganizadorRepository organizadorRepo;

	private final PresentacionRepository presentacionRepo;

	private final ModelMapper modelMapper;

	public FestivalService(FestivalRepository festivalRepo, OrganizadorRepository organizadorRepo,
			PresentacionRepository presentacionRepo, ModelMapper modelMapper) {
		this.festivalRepo = festivalRepo;
		this.organizadorRepo = organizadorRepo;
		this.presentacionRepo = presentacionRepo;
		this.modelMapper = modelMapper;
	}

	@Transactional
	public FestivalDTO crear(FestivalDTO dto) {

		validar(dto, 0);

		Festival festival = aEntidad(dto);
		festivalRepo.save(festival);

		return aDTO(festival);
	}

	@Transactional
	public FestivalDTO actualizar(long id, FestivalDTO dto) {

		buscarEntidad(id);
		validar(dto, id);

		for (Presentacion presentacion : presentacionRepo.findByFestivalId(id)) {
			if (presentacion.getFecha().isBefore(dto.getFechaInicio())
					|| presentacion.getFecha().isAfter(dto.getFechaFin())) {
				throw new ReglaNegocioException("Hay presentaciones programadas fuera de las nuevas fechas del festival");
			}
		}

		dto.setIdFestival(id);
		Festival festival = aEntidad(dto);
		festivalRepo.update(festival);

		return aDTO(festival);
	}

	@Transactional
	public void eliminar(long id) {

		buscarEntidad(id);

		try {
			festivalRepo.deleteById(id);
		} catch (DataIntegrityViolationException e) {
			throw new ReglaNegocioException(
					"No se puede eliminar el festival porque tiene escenarios, zonas, presentaciones o patrocinios asociados");
		}
	}

	public FestivalDTO buscarPorId(long id) {
		return aDTO(buscarEntidad(id));
	}

	public List<FestivalDTO> listar() {
		return festivalRepo.findAll().stream().map(this::aDTO).toList();
	}

	public List<FestivalDTO> listarPorOrganizador(long documentoOrganizador) {
		return festivalRepo.findByOrganizadorDocumento(documentoOrganizador).stream().map(this::aDTO).toList();
	}

	private Festival buscarEntidad(long id) {
		return festivalRepo.findById(id)
				.orElseThrow(() -> new RecursoNoEncontradoException("El festival " + id + " no existe"));
	}

	private void validar(FestivalDTO dto, long idPropio) {

		Validaciones.requerirTexto(dto.getNombre(), "nombre");
		Validaciones.requerirPositivo(dto.getEdicion(), "edicion");
		Validaciones.requerirNoNulo(dto.getFechaInicio(), "fechaInicio");
		Validaciones.requerirNoNulo(dto.getFechaFin(), "fechaFin");
		Validaciones.requerirTexto(dto.getUbicacionPais(), "ubicacionPais");
		Validaciones.requerirTexto(dto.getUbicacionCiudad(), "ubicacionCiudad");

		if (dto.getFechaFin().isBefore(dto.getFechaInicio())) {
			throw new IllegalArgumentException("La fecha de fin no puede ser anterior a la fecha de inicio");
		}

		if (!organizadorRepo.existsById(dto.getDocumentoOrganizador())) {
			throw new RecursoNoEncontradoException("El organizador " + dto.getDocumentoOrganizador() + " no existe");
		}

		for (Festival existente : festivalRepo.findByNombreAndEdicion(dto.getNombre().trim(), dto.getEdicion())) {
			if (existente.getIdFestival() != idPropio) {
				throw new ReglaNegocioException(
						"Ya existe la edición " + dto.getEdicion() + " del festival " + dto.getNombre().trim());
			}
		}
	}

	private FestivalDTO aDTO(Festival festival) {
		FestivalDTO dto = modelMapper.map(festival, FestivalDTO.class);

		if (festival.getUbicacion() != null) {
			dto.setUbicacionPais(festival.getUbicacion().getPais());
			dto.setUbicacionCiudad(festival.getUbicacion().getCiudad());
			dto.setUbicacionDireccion(festival.getUbicacion().getDireccion());
		}

		dto.setDocumentoOrganizador(festival.getOrganizador() != null ? festival.getOrganizador().getDocumento() : 0);
		return dto;
	}

	private Festival aEntidad(FestivalDTO dto) {
		Festival festival = modelMapper.map(dto, Festival.class);
		festival.setNombre(dto.getNombre().trim());
		festival.setUbicacion(new Ubicacion(dto.getUbicacionPais().trim(), dto.getUbicacionCiudad().trim(),
				dto.getUbicacionDireccion()));

		Organizador organizador = new Organizador();
		organizador.setDocumento(dto.getDocumentoOrganizador());
		festival.setOrganizador(organizador);

		return festival;
	}

}