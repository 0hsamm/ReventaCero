package co.edu.unbosque.reventacero.service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import co.edu.unbosque.reventacero.dto.VentaPrimariaDTO;
import co.edu.unbosque.reventacero.entity.Boleta;
import co.edu.unbosque.reventacero.entity.Dispositivo;
import co.edu.unbosque.reventacero.entity.Festival;
import co.edu.unbosque.reventacero.entity.TipoEntrada;
import co.edu.unbosque.reventacero.entity.Usuario;
import co.edu.unbosque.reventacero.entity.VentaPrimaria;
import co.edu.unbosque.reventacero.enums.TipoTransaccion;
import co.edu.unbosque.reventacero.exception.RecursoNoEncontradoException;
import co.edu.unbosque.reventacero.exception.ReglaNegocioException;
import co.edu.unbosque.reventacero.repository.BoletaRepository;
import co.edu.unbosque.reventacero.repository.TipoEntradaRepository;
import co.edu.unbosque.reventacero.repository.TransaccionRepository;
import co.edu.unbosque.reventacero.repository.VentaPrimariaRepository;
import co.edu.unbosque.reventacero.repository.ZonaRepository;
import co.edu.unbosque.reventacero.util.Validaciones;

@Service
public class VentaPrimariaService {

	private final VentaPrimariaRepository ventaRepo;

	private final TransaccionRepository transaccionRepo;

	private final BoletaRepository boletaRepo;

	private final TipoEntradaRepository tipoEntradaRepo;

	private final ZonaRepository zonaRepo;

	private final BoletaService boletaService;

	private final ZonaService zonaService;

	private final UsuarioService usuarioService;

	private final DispositivoService dispositivoService;

	private final TransaccionService transaccionService;

	private final ModelMapper modelMapper;

	public VentaPrimariaService(VentaPrimariaRepository ventaRepo, TransaccionRepository transaccionRepo,
			BoletaRepository boletaRepo, TipoEntradaRepository tipoEntradaRepo, ZonaRepository zonaRepo,
			BoletaService boletaService, ZonaService zonaService, UsuarioService usuarioService,
			DispositivoService dispositivoService, TransaccionService transaccionService, ModelMapper modelMapper) {
		this.ventaRepo = ventaRepo;
		this.transaccionRepo = transaccionRepo;
		this.boletaRepo = boletaRepo;
		this.tipoEntradaRepo = tipoEntradaRepo;
		this.zonaRepo = zonaRepo;
		this.boletaService = boletaService;
		this.zonaService = zonaService;
		this.usuarioService = usuarioService;
		this.dispositivoService = dispositivoService;
		this.transaccionService = transaccionService;
		this.modelMapper = modelMapper;
	}

	@Transactional
	public VentaPrimariaDTO comprar(VentaPrimariaDTO dto) {

		Validaciones.requerirPositivo(dto.getIdTipoEntrada(), "idTipoEntrada");
		Validaciones.requerirPositivo(dto.getIdDispositivo(), "idDispositivo");
		Validaciones.requerirTexto(dto.getMetodoPago(), "metodoPago");
		Validaciones.requerirTexto(dto.getCanalVenta(), "canalVenta");

		TipoEntrada tipoEntrada = tipoEntradaRepo.findById(dto.getIdTipoEntrada()).orElseThrow(
				() -> new RecursoNoEncontradoException("El tipo de entrada " + dto.getIdTipoEntrada() + " no existe"));

		if (tipoEntrada.getPrecioBase() == null) {
			throw new ReglaNegocioException("El tipo de entrada no tiene un precio definido");
		}

		Festival festival = boletaService.festivalDe(tipoEntrada);
		boletaService.validarFestivalNoFinalizado(festival);

		Dispositivo dispositivo = dispositivoService.validarDispositivoActivo(dto.getIdDispositivo());
		long documentoComprador = dispositivo.getUsuario().getDocumento();
		usuarioService.validarCuentaActiva(documentoComprador);

		long idZona = tipoEntrada.getZona().getIdZona();
		zonaRepo.bloquearPorId(idZona);

		if (zonaService.aforoDisponible(idZona) <= 0) {
			throw new ReglaNegocioException("No hay boletas disponibles en la zona de este tipo de entrada");
		}

		Usuario comprador = new Usuario();
		comprador.setDocumento(documentoComprador);

		Boleta boleta = new Boleta();
		boleta.setFechaEmision(LocalDate.now());
		boleta.setTipoEntrada(tipoEntrada);
		boleta.setComprador(comprador);
		boletaRepo.save(boleta);

		VentaPrimaria venta = new VentaPrimaria();
		venta.setFechaHora(LocalDateTime.now());
		venta.setPrecio(tipoEntrada.getPrecioBase());
		venta.setComisionOrganizador(BigDecimal.ZERO);
		venta.setMetodoPago(dto.getMetodoPago().trim());
		venta.setBoleta(boleta);
		venta.setDispositivo(dispositivo);
		venta.setTipoTransaccion(TipoTransaccion.VENTA_PRIMARIA);
		venta.setCanalVena(dto.getCanalVenta().trim());

		transaccionRepo.save(venta);
		ventaRepo.save(venta);

		return aDTO(venta);
	}

	public VentaPrimariaDTO buscarPorId(long id) {
		VentaPrimaria venta = ventaRepo.findById(id)
				.orElseThrow(() -> new RecursoNoEncontradoException("La venta primaria " + id + " no existe"));
		return aDTO(venta);
	}

	public List<VentaPrimariaDTO> listar() {
		return ventaRepo.findAll().stream().map(this::aDTO).toList();
	}

	public List<VentaPrimariaDTO> listarPorBoleta(long codigoBoleta) {
		return ventaRepo.findByBoletaCodigoBoleta(codigoBoleta).stream().map(this::aDTO).toList();
	}

	private VentaPrimariaDTO aDTO(VentaPrimaria venta) {
		VentaPrimariaDTO dto = modelMapper.map(venta, VentaPrimariaDTO.class);
		transaccionService.completarDTO(venta, dto);
		dto.setCanalVenta(venta.getCanalVena());
		dto.setIdTipoEntrada(boletaRepo.findById(dto.getCodigoBoleta())
				.map(b -> b.getTipoEntrada().getIdTipoEntrada()).orElse(0L));
		return dto;
	}

}