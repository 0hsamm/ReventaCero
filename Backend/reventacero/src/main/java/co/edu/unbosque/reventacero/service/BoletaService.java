package co.edu.unbosque.reventacero.service;

import java.time.LocalDate;
import java.util.List;

import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;

import co.edu.unbosque.reventacero.dto.BoletaDTO;
import co.edu.unbosque.reventacero.entity.Boleta;
import co.edu.unbosque.reventacero.entity.Festival;
import co.edu.unbosque.reventacero.entity.TipoEntrada;
import co.edu.unbosque.reventacero.entity.Zona;
import co.edu.unbosque.reventacero.exception.RecursoNoEncontradoException;
import co.edu.unbosque.reventacero.exception.ReglaNegocioException;
import co.edu.unbosque.reventacero.repository.BoletaRepository;
import co.edu.unbosque.reventacero.repository.ControlAccesoRepository;
import co.edu.unbosque.reventacero.repository.DevolucionRepository;
import co.edu.unbosque.reventacero.repository.FestivalRepository;
import co.edu.unbosque.reventacero.repository.TipoEntradaRepository;
import co.edu.unbosque.reventacero.repository.UsuarioRepository;
import co.edu.unbosque.reventacero.repository.ZonaRepository;

@Service
public class BoletaService {

	private final BoletaRepository boletaRepo;

	private final UsuarioRepository usuarioRepo;

	private final TipoEntradaRepository tipoEntradaRepo;

	private final ZonaRepository zonaRepo;

	private final FestivalRepository festivalRepo;

	private final ControlAccesoRepository controlAccesoRepo;

	private final DevolucionRepository devolucionRepo;

	private final ModelMapper modelMapper;

	public BoletaService(BoletaRepository boletaRepo, UsuarioRepository usuarioRepo,
			TipoEntradaRepository tipoEntradaRepo, ZonaRepository zonaRepo, FestivalRepository festivalRepo,
			ControlAccesoRepository controlAccesoRepo, DevolucionRepository devolucionRepo, ModelMapper modelMapper) {
		this.boletaRepo = boletaRepo;
		this.usuarioRepo = usuarioRepo;
		this.tipoEntradaRepo = tipoEntradaRepo;
		this.zonaRepo = zonaRepo;
		this.festivalRepo = festivalRepo;
		this.controlAccesoRepo = controlAccesoRepo;
		this.devolucionRepo = devolucionRepo;
		this.modelMapper = modelMapper;
	}

	public BoletaDTO buscarPorCodigo(long codigoBoleta) {
		return aDTO(buscarEntidad(codigoBoleta));
	}

	public List<BoletaDTO> listar() {
		return boletaRepo.findAll().stream().map(this::aDTO).toList();
	}

	public List<BoletaDTO> listarPorComprador(long documento) {
		if (!usuarioRepo.existsByDocumento(documento)) {
			throw new RecursoNoEncontradoException("El usuario " + documento + " no existe");
		}
		return boletaRepo.findByCompradorDocumento(documento).stream().map(this::aDTO).toList();
	}

	public List<BoletaDTO> listarPorTipoEntrada(long idTipoEntrada) {
		if (!tipoEntradaRepo.existsById(idTipoEntrada)) {
			throw new RecursoNoEncontradoException("El tipo de entrada " + idTipoEntrada + " no existe");
		}
		return boletaRepo.findByTipoEntradaId(idTipoEntrada).stream().map(this::aDTO).toList();
	}

	public Boleta bloquearYBuscar(long codigoBoleta) {
		boletaRepo.bloquearPorCodigo(codigoBoleta);
		return buscarEntidad(codigoBoleta);
	}

	public void validarDisponible(long codigoBoleta) {

		if (!devolucionRepo.findByBoletaCodigoBoleta(codigoBoleta).isEmpty()) {
			throw new ReglaNegocioException("La boleta " + codigoBoleta + " ya fue devuelta");
		}

		if (controlAccesoRepo.findByBoletaCodigoBoleta(codigoBoleta).isPresent()) {
			throw new ReglaNegocioException("La boleta " + codigoBoleta + " ya fue utilizada para ingresar");
		}
	}

	public Festival festivalDe(TipoEntrada tipoEntrada) {
		Zona zona = zonaRepo.findById(tipoEntrada.getZona().getIdZona()).orElseThrow(
				() -> new RecursoNoEncontradoException("La zona " + tipoEntrada.getZona().getIdZona() + " no existe"));
		return festivalRepo.findById(zona.getFestival().getIdFestival()).orElseThrow(
				() -> new RecursoNoEncontradoException(
						"El festival " + zona.getFestival().getIdFestival() + " no existe"));
	}

	public void validarFestivalNoFinalizado(Festival festival) {
		if (festival.getFechaFin() != null && festival.getFechaFin().isBefore(LocalDate.now())) {
			throw new ReglaNegocioException("El festival " + festival.getNombre() + " ya finalizó");
		}
	}

	private Boleta buscarEntidad(long codigoBoleta) {
		return boletaRepo.findById(codigoBoleta)
				.orElseThrow(() -> new RecursoNoEncontradoException("La boleta " + codigoBoleta + " no existe"));
	}

	private BoletaDTO aDTO(Boleta boleta) {
		BoletaDTO dto = modelMapper.map(boleta, BoletaDTO.class);
		dto.setIdTipoEntrada(boleta.getTipoEntrada() != null ? boleta.getTipoEntrada().getIdTipoEntrada() : 0);
		dto.setDocumentoComprador(boleta.getComprador() != null ? boleta.getComprador().getDocumento() : 0);
		return dto;
	}

}