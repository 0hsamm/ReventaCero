package co.edu.unbosque.reventacero.service;

import java.util.List;

import org.modelmapper.ModelMapper;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import co.edu.unbosque.reventacero.dto.TipoEntradaDTO;
import co.edu.unbosque.reventacero.entity.TipoEntrada;
import co.edu.unbosque.reventacero.entity.Zona;
import co.edu.unbosque.reventacero.exception.RecursoNoEncontradoException;
import co.edu.unbosque.reventacero.exception.ReglaNegocioException;
import co.edu.unbosque.reventacero.repository.BoletaRepository;
import co.edu.unbosque.reventacero.repository.TipoEntradaRepository;
import co.edu.unbosque.reventacero.repository.ZonaRepository;
import co.edu.unbosque.reventacero.util.Validaciones;

@Service
public class TipoEntradaService {

	private final TipoEntradaRepository tipoEntradaRepo;

	private final ZonaRepository zonaRepo;

	private final BoletaRepository boletaRepo;

	private final ModelMapper modelMapper;

	public TipoEntradaService(TipoEntradaRepository tipoEntradaRepo, ZonaRepository zonaRepo,
			BoletaRepository boletaRepo, ModelMapper modelMapper) {
		this.tipoEntradaRepo = tipoEntradaRepo;
		this.zonaRepo = zonaRepo;
		this.boletaRepo = boletaRepo;
		this.modelMapper = modelMapper;
	}

	@Transactional
	public TipoEntradaDTO crear(TipoEntradaDTO dto) {

		validar(dto, 0);

		TipoEntrada tipoEntrada = aEntidad(dto);
		tipoEntradaRepo.save(tipoEntrada);

		return aDTO(tipoEntrada);
	}

	@Transactional
	public TipoEntradaDTO actualizar(long id, TipoEntradaDTO dto) {

		TipoEntrada existente = buscarEntidad(id);
		validar(dto, id);

		// Con boletas vendidas no se puede mover el tipo de entrada a otra zona
		if (existente.getZona().getIdZona() != dto.getIdZona() && !boletaRepo.findByTipoEntradaId(id).isEmpty()) {
			throw new ReglaNegocioException("No se puede cambiar la zona de un tipo de entrada que ya tiene boletas");
		}

		dto.setIdTipoEntrada(id);
		TipoEntrada tipoEntrada = aEntidad(dto);
		tipoEntradaRepo.update(tipoEntrada);

		return aDTO(tipoEntrada);
	}

	@Transactional
	public void eliminar(long id) {

		buscarEntidad(id);

		if (!boletaRepo.findByTipoEntradaId(id).isEmpty()) {
			throw new ReglaNegocioException("No se puede eliminar el tipo de entrada porque ya tiene boletas");
		}

		try {
			tipoEntradaRepo.deleteById(id);
		} catch (DataIntegrityViolationException e) {
			throw new ReglaNegocioException(
					"No se puede eliminar el tipo de entrada porque tiene registros asociados (por ejemplo una lista de espera)");
		}
	}

	public TipoEntradaDTO buscarPorId(long id) {
		return aDTO(buscarEntidad(id));
	}

	public List<TipoEntradaDTO> listar() {
		return tipoEntradaRepo.findAll().stream().map(this::aDTO).toList();
	}

	public List<TipoEntradaDTO> listarPorZona(long idZona) {
		return tipoEntradaRepo.findByZonaId(idZona).stream().map(this::aDTO).toList();
	}

	private TipoEntrada buscarEntidad(long id) {
		return tipoEntradaRepo.findById(id)
				.orElseThrow(() -> new RecursoNoEncontradoException("El tipo de entrada " + id + " no existe"));
	}

	private void validar(TipoEntradaDTO dto, long idPropio) {

		Validaciones.requerirTexto(dto.getCategoria(), "categoria");
		Validaciones.requerirNoNegativo(dto.getPrecioBase(), "precioBase");
		Validaciones.requerirNoNegativo(dto.getPrecioTopeReventa(), "precioTopeReventa");

		if (!zonaRepo.existsById(dto.getIdZona())) {
			throw new RecursoNoEncontradoException("La zona " + dto.getIdZona() + " no existe");
		}

		for (TipoEntrada existente : tipoEntradaRepo.findByZonaId(dto.getIdZona())) {
			if (existente.getIdTipoEntrada() != idPropio
					&& existente.getCategoria() != null
					&& existente.getCategoria().equalsIgnoreCase(dto.getCategoria().trim())) {
				throw new ReglaNegocioException("La zona ya tiene un tipo de entrada con la categoría " + dto.getCategoria().trim());
			}
		}
	}

	private TipoEntradaDTO aDTO(TipoEntrada tipoEntrada) {
		TipoEntradaDTO dto = modelMapper.map(tipoEntrada, TipoEntradaDTO.class);
		dto.setIdZona(tipoEntrada.getZona() != null ? tipoEntrada.getZona().getIdZona() : 0);
		return dto;
	}

	private TipoEntrada aEntidad(TipoEntradaDTO dto) {
		TipoEntrada tipoEntrada = modelMapper.map(dto, TipoEntrada.class);
		tipoEntrada.setCategoria(dto.getCategoria().trim());
		Zona zona = new Zona();
		zona.setIdZona(dto.getIdZona());
		tipoEntrada.setZona(zona);
		return tipoEntrada;
	}

}