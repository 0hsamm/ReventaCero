package co.edu.unbosque.reventacero.service;

import java.math.BigDecimal;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import co.edu.unbosque.reventacero.dto.PublicacionReventaDTO;
import co.edu.unbosque.reventacero.dto.ReventaAutorizadaDTO;
import co.edu.unbosque.reventacero.entity.Boleta;
import co.edu.unbosque.reventacero.exception.ReglaNegocioException;
import co.edu.unbosque.reventacero.repository.PublicacionReventaRepository;
import co.edu.unbosque.reventacero.util.Validaciones;

/** La boleta sigue siendo del revendedor hasta que alguien la compra desde la pestaña de reventas. */
@Service
public class PublicacionReventaService {

	private static final String NO_DISPONIBLE = "Esta boleta ya no está disponible";

	private final PublicacionReventaRepository publicacionRepo;

	private final ReventaAutorizadaService reventaService;

	private final BoletaService boletaService;

	private final DispositivoService dispositivoService;

	public PublicacionReventaService(PublicacionReventaRepository publicacionRepo,
			ReventaAutorizadaService reventaService, BoletaService boletaService,
			DispositivoService dispositivoService) {
		this.publicacionRepo = publicacionRepo;
		this.reventaService = reventaService;
		this.boletaService = boletaService;
		this.dispositivoService = dispositivoService;
	}

	@Transactional
	public void publicar(long codigoBoleta, BigDecimal precio) {

		Validaciones.requerirPositivo(codigoBoleta, "codigoBoleta");
		Validaciones.requerirNoNegativo(precio, "precio");

		reventaService.validarBoletaRevendible(codigoBoleta, precio);

		if (publicacionRepo.publicar(codigoBoleta, precio) == 0) {
			throw new ReglaNegocioException("Esta boleta ya está publicada");
		}
	}

	@Transactional
	public void comprar(long referencia, long compradorDocumento, long idDispositivo, String metodoPago) {

		Validaciones.requerirTexto(metodoPago, "metodoPago");

		PublicacionReventaDTO publicacion = publicacionRepo.findByReferencia(referencia)
				.orElseThrow(() -> new ReglaNegocioException(NO_DISPONIBLE));

		if (publicacion.getVendedorDocumento() == compradorDocumento) {
			throw new ReglaNegocioException("No puedes comprar tu propia boleta");
		}

		// Con la boleta bloqueada, quien llegue segundo ve que ya cambió de dueño o dejó de estar en venta.
		Boleta boleta = boletaService.bloquearYBuscar(publicacion.getCodigoBoleta());

		if (boleta.getComprador().getDocumento() != publicacion.getVendedorDocumento()
				|| publicacionRepo.quitarDeVenta(publicacion.getCodigoBoleta()) == 0) {
			throw new ReglaNegocioException(NO_DISPONIBLE);
		}

		if (dispositivoService.buscarPorId(idDispositivo).getDocumentoUsuario() != compradorDocumento) {
			throw new ReglaNegocioException("El dispositivo debe pertenecer a quien compra la boleta");
		}

		// Si algo cambió desde que se publicó, la excepción deshace también el retiro de la venta.
		ReventaAutorizadaDTO reventa = new ReventaAutorizadaDTO();
		reventa.setCodigoBoleta(publicacion.getCodigoBoleta());
		reventa.setCompradorDocumento(compradorDocumento);
		reventa.setPrecio(publicacion.getPrecio());
		reventa.setMetodoPago(metodoPago);
		reventa.setIdDispositivo(idDispositivo);
		reventaService.revender(reventa);
	}

	@Transactional
	public void retirar(long codigoBoleta) {
		if (publicacionRepo.quitarDeVenta(codigoBoleta) == 0) {
			throw new ReglaNegocioException("Esta boleta no está publicada");
		}
	}

	/**
	 * Lo que ve quien busca comprar, incluso sin sesión. El código de la boleta es lo que da acceso al
	 * festival, y el documento es un dato personal: ninguno sale de aquí.
	 */
	public List<PublicacionReventaDTO> disponibles(long compradorDocumento) {
		List<PublicacionReventaDTO> disponibles = publicacionRepo.findDisponiblesParaComprador(compradorDocumento)
				.stream()
				.filter(publicacion -> publicacion.getReferencia() > 0)
				.toList();

		disponibles.forEach(publicacion -> {
			publicacion.setCodigoBoleta(0);
			publicacion.setVendedorDocumento(0);
		});

		return disponibles;
	}

	public List<PublicacionReventaDTO> delVendedor(long documento) {
		return publicacionRepo.findDelVendedor(documento);
	}
}
