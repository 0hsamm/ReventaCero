package co.edu.unbosque.reventacero.service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import co.edu.unbosque.reventacero.dto.DevolucionDTO;
import co.edu.unbosque.reventacero.entity.Boleta;
import co.edu.unbosque.reventacero.entity.Devolucion;
import co.edu.unbosque.reventacero.entity.Dispositivo;
import co.edu.unbosque.reventacero.entity.Festival;
import co.edu.unbosque.reventacero.entity.TipoEntrada;
import co.edu.unbosque.reventacero.entity.Transaccion;
import co.edu.unbosque.reventacero.enums.TipoTransaccion;
import co.edu.unbosque.reventacero.exception.RecursoNoEncontradoException;
import co.edu.unbosque.reventacero.exception.ReglaNegocioException;
import co.edu.unbosque.reventacero.repository.BoletaRepository;
import co.edu.unbosque.reventacero.repository.DevolucionRepository;
import co.edu.unbosque.reventacero.repository.SolicitudReventaRepository;
import co.edu.unbosque.reventacero.repository.TipoEntradaRepository;
import co.edu.unbosque.reventacero.repository.TransaccionRepository;
import co.edu.unbosque.reventacero.util.Validaciones;

@Service
public class DevolucionService {

	private static final BigDecimal CIEN = BigDecimal.valueOf(100);

	private final DevolucionRepository devolucionRepo;

	private final TransaccionRepository transaccionRepo;

	private final BoletaRepository boletaRepo;

	private final TipoEntradaRepository tipoEntradaRepo;

	private final BoletaService boletaService;

	private final UsuarioService usuarioService;

	private final DispositivoService dispositivoService;

	private final TransaccionService transaccionService;
	
	private final NotificacionService notificacionService;

	private final SolicitudReventaRepository solicitudRepo;

	private final ModelMapper modelMapper;


	public DevolucionService(DevolucionRepository devolucionRepo, TransaccionRepository transaccionRepo,
			BoletaRepository boletaRepo, TipoEntradaRepository tipoEntradaRepo, BoletaService boletaService,
			UsuarioService usuarioService, DispositivoService dispositivoService,
			TransaccionService transaccionService, NotificacionService notificacionService,
			SolicitudReventaRepository solicitudRepo, ModelMapper modelMapper) {
		this.devolucionRepo = devolucionRepo;
		this.transaccionRepo = transaccionRepo;
		this.boletaRepo = boletaRepo;
		this.tipoEntradaRepo = tipoEntradaRepo;
		this.boletaService = boletaService;
		this.usuarioService = usuarioService;
		this.dispositivoService = dispositivoService;
		this.transaccionService = transaccionService;
		this.notificacionService = notificacionService;
		this.solicitudRepo = solicitudRepo;
		this.modelMapper = modelMapper;
	}

	@Transactional
	public DevolucionDTO devolver(DevolucionDTO dto) {

		Validaciones.requerirPositivo(dto.getCodigoBoleta(), "codigoBoleta");
		Validaciones.requerirPositivo(dto.getIdDispositivo(), "idDispositivo");
		Validaciones.requerirTexto(dto.getMotivo(), "motivo");
		Validaciones.requerirTexto(dto.getMetodoPago(), "metodoPago");
		Validaciones.requerirNoNulo(dto.getPorcentajeReembolso(), "porcentajeReembolso");

		if (dto.getPorcentajeReembolso().signum() <= 0 || dto.getPorcentajeReembolso().compareTo(CIEN) > 0) {
			throw new IllegalArgumentException("El porcentaje de reembolso debe estar entre 0 (sin incluir) y 100");
		}

		Boleta boleta = boletaService.bloquearYBuscar(dto.getCodigoBoleta());
		long propietario = boleta.getComprador().getDocumento();

		usuarioService.validarCuentaActiva(propietario);
		boletaService.validarDisponible(boleta.getCodigoBoleta());

		TipoEntrada tipoEntrada = tipoEntradaRepo.findById(boleta.getTipoEntrada().getIdTipoEntrada()).orElseThrow(
				() -> new RecursoNoEncontradoException(
						"El tipo de entrada " + boleta.getTipoEntrada().getIdTipoEntrada() + " no existe"));

		Festival festival = boletaService.festivalDe(tipoEntrada);
		if (festival.getFechaInicio() != null && !festival.getFechaInicio().isAfter(LocalDate.now())) {
			throw new ReglaNegocioException("El festival ya inició; no se permiten devoluciones");
		}

		Dispositivo dispositivo = dispositivoService.validarDispositivoActivo(dto.getIdDispositivo());
		if (dispositivo.getUsuario().getDocumento() != propietario) {
			throw new ReglaNegocioException("El dispositivo no pertenece al propietario de la boleta");
		}

		List<Transaccion> historial = transaccionRepo.findByBoletaCodigoBoleta(boleta.getCodigoBoleta());
		if (historial.isEmpty() || historial.get(historial.size() - 1).getPrecio() == null) {
			throw new ReglaNegocioException("La boleta no tiene un pago registrado que se pueda reembolsar");
		}

		BigDecimal precioPagado = historial.get(historial.size() - 1).getPrecio();

		Devolucion devolucion = new Devolucion();
		devolucion.setFechaHora(LocalDateTime.now());
		devolucion.setPrecio(precioPagado.multiply(dto.getPorcentajeReembolso()).divide(CIEN, 2, RoundingMode.HALF_UP));
		devolucion.setComisionOrganizador(BigDecimal.ZERO);
		devolucion.setMetodoPago(dto.getMetodoPago().trim());
		devolucion.setBoleta(boleta);
		devolucion.setDispositivo(dispositivo);
		devolucion.setTipoTransaccion(TipoTransaccion.DEVOLUCION);
		devolucion.setMotivo(dto.getMotivo().trim());
		devolucion.setPorcentajeReembolso(dto.getPorcentajeReembolso());

		transaccionRepo.save(devolucion);
		devolucionRepo.save(devolucion);
		// Una boleta devuelta ya no se puede transferir: su oferta pendiente deja de tener sentido
		solicitudRepo.cancelarPendientesDeBoleta(boleta.getCodigoBoleta(), SolicitudReventaService.CANCELADA,
				LocalDateTime.now());
		notificacionService.notificarCupoLiberado(tipoEntrada.getIdTipoEntrada());

		return aDTO(devolucion);
	}

	public DevolucionDTO buscarPorId(long id) {
		return aDTO(devolucionRepo.findById(id)
				.orElseThrow(() -> new RecursoNoEncontradoException("La devolución " + id + " no existe")));
	}

	public List<DevolucionDTO> listar() {
		return devolucionRepo.findAll().stream().map(this::aDTO).toList();
	}

	public List<DevolucionDTO> listarPorBoleta(long codigoBoleta) {
		if (!boletaRepo.existsById(codigoBoleta)) {
			throw new RecursoNoEncontradoException("La boleta " + codigoBoleta + " no existe");
		}
		return devolucionRepo.findByBoletaCodigoBoleta(codigoBoleta).stream().map(this::aDTO).toList();
	}

	private DevolucionDTO aDTO(Devolucion devolucion) {
		DevolucionDTO dto = modelMapper.map(devolucion, DevolucionDTO.class);
		transaccionService.completarDTO(devolucion, dto);
		return dto;
	}

}