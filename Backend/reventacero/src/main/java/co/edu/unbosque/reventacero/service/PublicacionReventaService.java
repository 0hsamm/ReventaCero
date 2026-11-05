package co.edu.unbosque.reventacero.service;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import co.edu.unbosque.reventacero.dto.PublicacionReventaDTO;
import co.edu.unbosque.reventacero.dto.ReventaAutorizadaDTO;
import co.edu.unbosque.reventacero.entity.Boleta;
import co.edu.unbosque.reventacero.exception.RecursoNoEncontradoException;
import co.edu.unbosque.reventacero.exception.ReglaNegocioException;
import co.edu.unbosque.reventacero.repository.PublicacionReventaRepository;
import co.edu.unbosque.reventacero.repository.SolicitudReventaRepository;
import co.edu.unbosque.reventacero.util.Validaciones;

/** La boleta sigue siendo del revendedor hasta que alguien la compra desde la página de reventas. */
@Service
public class PublicacionReventaService {

	private static final String YA_PUBLICADA = "Esta boleta ya está publicada";
	private static final String NO_DISPONIBLE = "Esta boleta ya no está disponible";

	private final PublicacionReventaRepository publicacionRepo;

	private final SolicitudReventaRepository solicitudRepo;

	private final ReventaAutorizadaService reventaService;

	private final BoletaService boletaService;

	private final DispositivoService dispositivoService;

	public PublicacionReventaService(PublicacionReventaRepository publicacionRepo,
			SolicitudReventaRepository solicitudRepo, ReventaAutorizadaService reventaService,
			BoletaService boletaService, DispositivoService dispositivoService) {
		this.publicacionRepo = publicacionRepo;
		this.solicitudRepo = solicitudRepo;
		this.reventaService = reventaService;
		this.boletaService = boletaService;
		this.dispositivoService = dispositivoService;
	}

	@Transactional
	public PublicacionReventaDTO publicar(PublicacionReventaDTO dto, long vendedorDocumento) {

		Validaciones.requerirPositivo(dto.getCodigoBoleta(), "codigoBoleta");
		Validaciones.requerirNoNegativo(dto.getPrecio(), "precio");

		reventaService.validarBoletaRevendible(dto.getCodigoBoleta(), dto.getPrecio());

		if (solicitudRepo.existePendiente(dto.getCodigoBoleta())) {
			throw new ReglaNegocioException("Esta boleta tiene una oferta pendiente; cancélala antes de publicarla");
		}

		if (publicacionRepo.existeDisponible(dto.getCodigoBoleta())) {
			throw new ReglaNegocioException(YA_PUBLICADA);
		}

		dto.setVendedorDocumento(vendedorDocumento);
		dto.setFechaPublicacion(LocalDateTime.now());

		try {
			return buscarPorId(publicacionRepo.save(dto));
		} catch (DuplicateKeyException e) {
			throw new ReglaNegocioException(YA_PUBLICADA);
		}
	}

	@Transactional
	public PublicacionReventaDTO comprar(long id, long compradorDocumento, long idDispositivo, String metodoPago) {

		Validaciones.requerirTexto(metodoPago, "metodoPago");

		PublicacionReventaDTO publicacion = buscarPorId(id);

		if (publicacion.getVendedorDocumento() == compradorDocumento) {
			throw new ReglaNegocioException("No puedes comprar tu propia boleta");
		}

		// Primero la boleta, como en las demás operaciones, para no cruzar bloqueos.
		Boleta boleta = boletaService.bloquearYBuscar(publicacion.getCodigoBoleta());

		if (publicacionRepo.venderSiDisponible(id, compradorDocumento, LocalDateTime.now()) == 0) {
			throw new ReglaNegocioException(NO_DISPONIBLE);
		}

		if (boleta.getComprador().getDocumento() != publicacion.getVendedorDocumento()) {
			throw new ReglaNegocioException(NO_DISPONIBLE);
		}

		if (dispositivoService.buscarPorId(idDispositivo).getDocumentoUsuario() != compradorDocumento) {
			throw new ReglaNegocioException("El dispositivo debe pertenecer a quien compra la boleta");
		}

		// Si algo cambió desde que se publicó, la excepción deshace también la venta.
		ReventaAutorizadaDTO reventa = new ReventaAutorizadaDTO();
		reventa.setCodigoBoleta(publicacion.getCodigoBoleta());
		reventa.setCompradorDocumento(compradorDocumento);
		reventa.setPrecio(publicacion.getPrecio());
		reventa.setMetodoPago(metodoPago);
		reventa.setIdDispositivo(idDispositivo);
		reventaService.revender(reventa);

		return buscarPorId(id);
	}

	@Transactional
	public PublicacionReventaDTO retirar(long id) {

		buscarPorId(id);

		if (publicacionRepo.retirarSiDisponible(id, LocalDateTime.now()) == 0) {
			throw new ReglaNegocioException("Esta publicación ya no está activa");
		}

		return buscarPorId(id);
	}

	public PublicacionReventaDTO buscarPorId(long id) {
		return publicacionRepo.findById(id)
				.orElseThrow(() -> new RecursoNoEncontradoException("La publicación " + id + " no existe"));
	}

	/**
	 * Lo que ve quien busca comprar. El código de la boleta es lo que da acceso al festival, y el
	 * documento es un dato personal: ninguno sale de aquí.
	 */
	public List<PublicacionReventaDTO> disponibles(long compradorDocumento) {
		List<PublicacionReventaDTO> disponibles = publicacionRepo.findDisponiblesParaComprador(compradorDocumento);

		disponibles.forEach(publicacion -> {
			publicacion.setCodigoBoleta(0);
			publicacion.setVendedorDocumento(0);
		});

		return disponibles;
	}

	public List<PublicacionReventaDTO> delVendedor(long documento) {
		return publicacionRepo.findByVendedor(documento);
	}
}
