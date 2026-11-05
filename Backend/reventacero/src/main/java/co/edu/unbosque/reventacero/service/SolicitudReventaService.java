package co.edu.unbosque.reventacero.service;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import co.edu.unbosque.reventacero.dto.ReventaAutorizadaDTO;
import co.edu.unbosque.reventacero.dto.SolicitudReventaDTO;
import co.edu.unbosque.reventacero.entity.Boleta;
import co.edu.unbosque.reventacero.exception.RecursoNoEncontradoException;
import co.edu.unbosque.reventacero.exception.ReglaNegocioException;
import co.edu.unbosque.reventacero.repository.PublicacionReventaRepository;
import co.edu.unbosque.reventacero.repository.SolicitudReventaRepository;
import co.edu.unbosque.reventacero.util.Validaciones;

/** La reventa solo se registra, y la boleta solo cambia de dueño, cuando el receptor acepta. */
@Service
public class SolicitudReventaService {

	public static final String ACEPTADA = "ACEPTADA";
	public static final String RECHAZADA = "RECHAZADA";
	public static final String CANCELADA = "CANCELADA";

	private static final String BOLETA_PENDIENTE = "Esta boleta ya tiene una solicitud de reventa pendiente";
	private static final String YA_RESPONDIDA = "Esta solicitud ya no está pendiente";

	private final SolicitudReventaRepository solicitudRepo;

	private final PublicacionReventaRepository publicacionRepo;

	private final ReventaAutorizadaService reventaService;

	private final BoletaService boletaService;

	private final DispositivoService dispositivoService;

	public SolicitudReventaService(SolicitudReventaRepository solicitudRepo,
			PublicacionReventaRepository publicacionRepo, ReventaAutorizadaService reventaService,
			BoletaService boletaService, DispositivoService dispositivoService) {
		this.solicitudRepo = solicitudRepo;
		this.publicacionRepo = publicacionRepo;
		this.reventaService = reventaService;
		this.boletaService = boletaService;
		this.dispositivoService = dispositivoService;
	}

	@Transactional
	public SolicitudReventaDTO crear(SolicitudReventaDTO dto, long vendedorDocumento) {

		Validaciones.requerirPositivo(dto.getCodigoBoleta(), "codigoBoleta");
		Validaciones.requerirPositivo(dto.getCompradorDocumento(), "compradorDocumento");
		Validaciones.requerirNoNegativo(dto.getPrecio(), "precio");
		Validaciones.requerirTexto(dto.getMetodoPago(), "metodoPago");

		reventaService.validarCondiciones(dto.getCodigoBoleta(), dto.getCompradorDocumento(), dto.getPrecio());

		if (solicitudRepo.existePendiente(dto.getCodigoBoleta())) {
			throw new ReglaNegocioException(BOLETA_PENDIENTE);
		}

		if (publicacionRepo.existeDisponible(dto.getCodigoBoleta())) {
			throw new ReglaNegocioException("Esta boleta está publicada; retírala antes de ofrecerla a alguien");
		}

		dto.setVendedorDocumento(vendedorDocumento);
		dto.setFechaHora(LocalDateTime.now());
		dto.setMetodoPago(dto.getMetodoPago().trim());

		try {
			return buscarPorId(solicitudRepo.save(dto));
		} catch (DuplicateKeyException e) {
			throw new ReglaNegocioException(BOLETA_PENDIENTE);
		}
	}

	@Transactional
	public SolicitudReventaDTO aceptar(long id, long idDispositivo) {

		SolicitudReventaDTO solicitud = buscarPorId(id);

		// Primero la boleta, como en las demás operaciones, para no cruzar bloqueos.
		Boleta boleta = boletaService.bloquearYBuscar(solicitud.getCodigoBoleta());

		if (solicitudRepo.resolverSiPendiente(id, ACEPTADA, LocalDateTime.now()) == 0) {
			throw new ReglaNegocioException(YA_RESPONDIDA);
		}

		if (boleta.getComprador().getDocumento() != solicitud.getVendedorDocumento()) {
			throw new ReglaNegocioException("La boleta ya no pertenece a quien la ofreció");
		}

		if (dispositivoService.buscarPorId(idDispositivo).getDocumentoUsuario() != solicitud.getCompradorDocumento()) {
			throw new ReglaNegocioException("El dispositivo debe pertenecer a quien acepta la reventa");
		}

		// Si algo cambió desde la oferta, la excepción deshace también el cambio de estado.
		ReventaAutorizadaDTO reventa = new ReventaAutorizadaDTO();
		reventa.setCodigoBoleta(solicitud.getCodigoBoleta());
		reventa.setCompradorDocumento(solicitud.getCompradorDocumento());
		reventa.setPrecio(solicitud.getPrecio());
		reventa.setMetodoPago(solicitud.getMetodoPago());
		reventa.setIdDispositivo(idDispositivo);
		reventaService.revender(reventa);

		return buscarPorId(id);
	}

	@Transactional
	public SolicitudReventaDTO rechazar(long id) {
		return cerrar(id, RECHAZADA);
	}

	@Transactional
	public SolicitudReventaDTO cancelar(long id) {
		return cerrar(id, CANCELADA);
	}

	public SolicitudReventaDTO buscarPorId(long id) {
		return solicitudRepo.findById(id)
				.orElseThrow(() -> new RecursoNoEncontradoException("La solicitud de reventa " + id + " no existe"));
	}

	public List<SolicitudReventaDTO> recibidas(long documento) {
		return solicitudRepo.findByComprador(documento);
	}

	public List<SolicitudReventaDTO> enviadas(long documento) {
		return solicitudRepo.findByVendedor(documento);
	}

	private SolicitudReventaDTO cerrar(long id, String estado) {

		buscarPorId(id);

		if (solicitudRepo.resolverSiPendiente(id, estado, LocalDateTime.now()) == 0) {
			throw new ReglaNegocioException(YA_RESPONDIDA);
		}

		return buscarPorId(id);
	}
}
