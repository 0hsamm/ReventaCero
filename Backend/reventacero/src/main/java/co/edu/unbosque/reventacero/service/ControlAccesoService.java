package co.edu.unbosque.reventacero.service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import co.edu.unbosque.reventacero.dto.ControlAccesoDTO;
import co.edu.unbosque.reventacero.entity.Boleta;
import co.edu.unbosque.reventacero.entity.ControlAcceso;
import co.edu.unbosque.reventacero.entity.Festival;
import co.edu.unbosque.reventacero.entity.TipoEntrada;
import co.edu.unbosque.reventacero.exception.RecursoNoEncontradoException;
import co.edu.unbosque.reventacero.repository.BoletaRepository;
import co.edu.unbosque.reventacero.repository.ControlAccesoRepository;
import co.edu.unbosque.reventacero.repository.DevolucionRepository;
import co.edu.unbosque.reventacero.repository.TipoEntradaRepository;
import co.edu.unbosque.reventacero.repository.UsuarioRepository;
import co.edu.unbosque.reventacero.util.Validaciones;

@Service
public class ControlAccesoService {

	public static final String PERMITIDO = "PERMITIDO";

	public static final String DENEGADO = "DENEGADO";

	private final ControlAccesoRepository controlAccesoRepo;

	private final DevolucionRepository devolucionRepo;

	private final TipoEntradaRepository tipoEntradaRepo;

	private final UsuarioRepository usuarioRepo;

	private final BoletaRepository boletaRepo;

	private final BoletaService boletaService;

	private final ModelMapper modelMapper;

	public ControlAccesoService(ControlAccesoRepository controlAccesoRepo, DevolucionRepository devolucionRepo,
			TipoEntradaRepository tipoEntradaRepo, UsuarioRepository usuarioRepo, BoletaRepository boletaRepo,
			BoletaService boletaService, ModelMapper modelMapper) {
		this.controlAccesoRepo = controlAccesoRepo;
		this.devolucionRepo = devolucionRepo;
		this.tipoEntradaRepo = tipoEntradaRepo;
		this.usuarioRepo = usuarioRepo;
		this.boletaRepo = boletaRepo;
		this.boletaService = boletaService;
		this.modelMapper = modelMapper;
	}

	@Transactional
	public ControlAccesoDTO registrarIngreso(long codigoBoleta) {

		Validaciones.requerirPositivo(codigoBoleta, "codigoBoleta");

		Boleta boleta = boletaService.bloquearYBuscar(codigoBoleta);

		if (controlAccesoRepo.findByBoletaCodigoBoleta(codigoBoleta).isPresent()) {
			return denegadoSinRegistro(codigoBoleta, "La boleta ya fue utilizada para ingresar");
		}

		if (!devolucionRepo.findByBoletaCodigoBoleta(codigoBoleta).isEmpty()) {
			return registrar(boleta, DENEGADO, "La boleta fue devuelta");
		}

		TipoEntrada tipoEntrada = tipoEntradaRepo.findById(boleta.getTipoEntrada().getIdTipoEntrada()).orElseThrow(
				() -> new RecursoNoEncontradoException(
						"El tipo de entrada " + boleta.getTipoEntrada().getIdTipoEntrada() + " no existe"));

		Festival festival = boletaService.festivalDe(tipoEntrada);
		LocalDate hoy = LocalDate.now();

		if (festival.getFechaInicio() != null && hoy.isBefore(festival.getFechaInicio())) {
			return denegadoSinRegistro(codigoBoleta, "El festival aún no ha comenzado");
		}

		if (festival.getFechaFin() != null && hoy.isAfter(festival.getFechaFin())) {
			return denegadoSinRegistro(codigoBoleta, "El festival ya finalizó");
		}

		boolean propietarioActivo = usuarioRepo.findByDocumento(boleta.getComprador().getDocumento())
				.map(u -> UsuarioService.ESTADO_ACTIVA.equalsIgnoreCase(u.getEstadoCuenta())).orElse(false);

		if (!propietarioActivo) {
			return denegadoSinRegistro(codigoBoleta, "La cuenta del propietario de la boleta no está activa");
		}

		return registrar(boleta, PERMITIDO, "Ingreso autorizado");
	}

	public ControlAccesoDTO buscarPorId(long id) {
		return aDTO(controlAccesoRepo.findById(id)
				.orElseThrow(() -> new RecursoNoEncontradoException("El registro de acceso " + id + " no existe")), null);
	}

	public ControlAccesoDTO buscarPorBoleta(long codigoBoleta) {
		if (!boletaRepo.existsById(codigoBoleta)) {
			throw new RecursoNoEncontradoException("La boleta " + codigoBoleta + " no existe");
		}
		return aDTO(controlAccesoRepo.findByBoletaCodigoBoleta(codigoBoleta).orElseThrow(
				() -> new RecursoNoEncontradoException("La boleta " + codigoBoleta + " no tiene registro de acceso")),
				null);
	}

	public List<ControlAccesoDTO> listar() {
		return controlAccesoRepo.findAll().stream().map(a -> aDTO(a, null)).toList();
	}

	private ControlAccesoDTO registrar(Boleta boleta, String resultado, String motivo) {
		ControlAcceso acceso = new ControlAcceso();
		acceso.setFechaHoraIngreso(LocalDateTime.now());
		acceso.setResultado(resultado);
		acceso.setBoleta(boleta);
		controlAccesoRepo.save(acceso);
		return aDTO(acceso, motivo);
	}

	private ControlAccesoDTO denegadoSinRegistro(long codigoBoleta, String motivo) {
		ControlAccesoDTO dto = new ControlAccesoDTO();
		dto.setFechaHoraIngreso(LocalDateTime.now());
		dto.setResultado(DENEGADO);
		dto.setCodigoBoleta(codigoBoleta);
		dto.setMotivo(motivo);
		return dto;
	}

	private ControlAccesoDTO aDTO(ControlAcceso acceso, String motivo) {
		ControlAccesoDTO dto = modelMapper.map(acceso, ControlAccesoDTO.class);
		dto.setCodigoBoleta(acceso.getBoleta() != null ? acceso.getBoleta().getCodigoBoleta() : 0);
		dto.setMotivo(motivo);
		return dto;
	}

}