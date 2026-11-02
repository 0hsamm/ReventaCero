package co.edu.unbosque.reventacero.service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.List;

import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import co.edu.unbosque.reventacero.dto.ReventaAutorizadaDTO;
import co.edu.unbosque.reventacero.entity.Boleta;
import co.edu.unbosque.reventacero.entity.Dispositivo;
import co.edu.unbosque.reventacero.entity.ReventaAutorizada;
import co.edu.unbosque.reventacero.entity.TipoEntrada;
import co.edu.unbosque.reventacero.entity.Usuario;
import co.edu.unbosque.reventacero.enums.TipoTransaccion;
import co.edu.unbosque.reventacero.exception.RecursoNoEncontradoException;
import co.edu.unbosque.reventacero.exception.ReglaNegocioException;
import co.edu.unbosque.reventacero.repository.BoletaRepository;
import co.edu.unbosque.reventacero.repository.FestivalRepository;
import co.edu.unbosque.reventacero.repository.ReventaAutorizadaRepository;
import co.edu.unbosque.reventacero.repository.TipoEntradaRepository;
import co.edu.unbosque.reventacero.repository.TransaccionRepository;
import co.edu.unbosque.reventacero.repository.UsuarioRepository;
import co.edu.unbosque.reventacero.util.Validaciones;

@Service
public class ReventaAutorizadaService {

	private static final BigDecimal CIEN = BigDecimal.valueOf(100);

	private final ReventaAutorizadaRepository reventaRepo;

	private final TransaccionRepository transaccionRepo;

	private final BoletaRepository boletaRepo;

	private final TipoEntradaRepository tipoEntradaRepo;

	private final UsuarioRepository usuarioRepo;

	private final FestivalRepository festivalRepo;

	private final BoletaService boletaService;

	private final UsuarioService usuarioService;

	private final DispositivoService dispositivoService;

	private final TransaccionService transaccionService;

	private final ModelMapper modelMapper;

	private final BigDecimal comisionPorcentaje;

	public ReventaAutorizadaService(ReventaAutorizadaRepository reventaRepo, TransaccionRepository transaccionRepo,
			BoletaRepository boletaRepo, TipoEntradaRepository tipoEntradaRepo, UsuarioRepository usuarioRepo,
			FestivalRepository festivalRepo, BoletaService boletaService, UsuarioService usuarioService,
			DispositivoService dispositivoService, TransaccionService transaccionService, ModelMapper modelMapper,
			@Value("${reventacero.reventa.comision-porcentaje:10}") BigDecimal comisionPorcentaje) {
		this.reventaRepo = reventaRepo;
		this.transaccionRepo = transaccionRepo;
		this.boletaRepo = boletaRepo;
		this.tipoEntradaRepo = tipoEntradaRepo;
		this.usuarioRepo = usuarioRepo;
		this.festivalRepo = festivalRepo;
		this.boletaService = boletaService;
		this.usuarioService = usuarioService;
		this.dispositivoService = dispositivoService;
		this.transaccionService = transaccionService;
		this.modelMapper = modelMapper;
		this.comisionPorcentaje = comisionPorcentaje;
	}

	@Transactional
	public ReventaAutorizadaDTO revender(ReventaAutorizadaDTO dto) {

		Validaciones.requerirPositivo(dto.getCodigoBoleta(), "codigoBoleta");
		Validaciones.requerirPositivo(dto.getCompradorDocumento(), "compradorDocumento");
		Validaciones.requerirPositivo(dto.getIdDispositivo(), "idDispositivo");
		Validaciones.requerirNoNegativo(dto.getPrecio(), "precio");
		Validaciones.requerirTexto(dto.getMetodoPago(), "metodoPago");

		Boleta boleta = boletaService.bloquearYBuscar(dto.getCodigoBoleta());
		long documentoVendedor = boleta.getComprador().getDocumento();

		if (documentoVendedor == dto.getCompradorDocumento()) {
			throw new ReglaNegocioException("El comprador no puede ser el mismo vendedor de la boleta");
		}

		usuarioService.validarCuentaActiva(documentoVendedor);
		usuarioService.validarCuentaActiva(dto.getCompradorDocumento());
		boletaService.validarDisponible(boleta.getCodigoBoleta());

		TipoEntrada tipoEntrada = tipoEntradaRepo.findById(boleta.getTipoEntrada().getIdTipoEntrada()).orElseThrow(
				() -> new RecursoNoEncontradoException(
						"El tipo de entrada " + boleta.getTipoEntrada().getIdTipoEntrada() + " no existe"));

		boletaService.validarFestivalNoFinalizado(boletaService.festivalDe(tipoEntrada));

		BigDecimal tope = tipoEntrada.getPrecioTopeReventa();
		if (tope == null) {
			throw new ReglaNegocioException("El tipo de entrada no tiene un tope de reventa definido");
		}
		if (dto.getPrecio().compareTo(tope) > 0) {
			throw new ReglaNegocioException("El precio supera el tope de reventa permitido (" + tope + ")");
		}

		Dispositivo dispositivo = dispositivoService.validarDispositivoActivo(dto.getIdDispositivo());
		long propietarioDispositivo = dispositivo.getUsuario().getDocumento();
		if (propietarioDispositivo != documentoVendedor && propietarioDispositivo != dto.getCompradorDocumento()) {
			throw new ReglaNegocioException("El dispositivo debe pertenecer al vendedor o al comprador");
		}

		Usuario vendedor = new Usuario();
		vendedor.setDocumento(documentoVendedor);
		Usuario comprador = new Usuario();
		comprador.setDocumento(dto.getCompradorDocumento());

		ReventaAutorizada reventa = new ReventaAutorizada();
		reventa.setFechaHora(LocalDateTime.now());
		reventa.setPrecio(dto.getPrecio());
		reventa.setComisionOrganizador(
				dto.getPrecio().multiply(comisionPorcentaje).divide(CIEN, 2, RoundingMode.HALF_UP));
		reventa.setMetodoPago(dto.getMetodoPago().trim());
		reventa.setBoleta(boleta);
		reventa.setDispositivo(dispositivo);
		reventa.setTipoTransaccion(TipoTransaccion.REVENTA_AUTORIZADA);
		reventa.setVendedor(vendedor);
		reventa.setComprador(comprador);

		transaccionRepo.save(reventa);
		reventaRepo.save(reventa);
		boletaRepo.actualizarComprador(boleta.getCodigoBoleta(), dto.getCompradorDocumento());

		return aDTO(reventa);
	}

	public ReventaAutorizadaDTO buscarPorId(long id) {
		return aDTO(reventaRepo.findById(id)
				.orElseThrow(() -> new RecursoNoEncontradoException("La reventa " + id + " no existe")));
	}

	public List<ReventaAutorizadaDTO> listar() {
		return reventaRepo.findAll().stream().map(this::aDTO).toList();
	}

	public List<ReventaAutorizadaDTO> listarPorBoleta(long codigoBoleta) {
		if (!boletaRepo.existsById(codigoBoleta)) {
			throw new RecursoNoEncontradoException("La boleta " + codigoBoleta + " no existe");
		}
		return reventaRepo.findByBoletaCodigoBoleta(codigoBoleta).stream().map(this::aDTO).toList();
	}

	public List<ReventaAutorizadaDTO> listarPorVendedor(long documento) {
		validarUsuario(documento);
		return reventaRepo.findByVendedorDocumento(documento).stream().map(this::aDTO).toList();
	}

	public List<ReventaAutorizadaDTO> listarPorComprador(long documento) {
		validarUsuario(documento);
		return reventaRepo.findByCompradorDocumento(documento).stream().map(this::aDTO).toList();
	}

	public List<ReventaAutorizadaDTO> listarPorFestival(long idFestival) {
		if (!festivalRepo.existsById(idFestival)) {
			throw new RecursoNoEncontradoException("El festival " + idFestival + " no existe");
		}
		return reventaRepo.findByFestivalId(idFestival).stream().map(this::aDTO).toList();
	}

	private void validarUsuario(long documento) {
		if (!usuarioRepo.existsByDocumento(documento)) {
			throw new RecursoNoEncontradoException("El usuario " + documento + " no existe");
		}
	}

	private ReventaAutorizadaDTO aDTO(ReventaAutorizada reventa) {
		ReventaAutorizadaDTO dto = modelMapper.map(reventa, ReventaAutorizadaDTO.class);
		transaccionService.completarDTO(reventa, dto);
		dto.setVendedorDocumento(reventa.getVendedor() != null ? reventa.getVendedor().getDocumento() : 0);
		dto.setCompradorDocumento(reventa.getComprador() != null ? reventa.getComprador().getDocumento() : 0);
		return dto;
	}

}