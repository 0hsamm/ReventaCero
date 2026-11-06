package co.edu.unbosque.reventacero.service;

import java.util.List;

import org.modelmapper.ModelMapper;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import co.edu.unbosque.reventacero.dto.DispositivoDTO;
import co.edu.unbosque.reventacero.entity.Dispositivo;
import co.edu.unbosque.reventacero.entity.Usuario;
import co.edu.unbosque.reventacero.exception.RecursoNoEncontradoException;
import co.edu.unbosque.reventacero.exception.ReglaNegocioException;
import co.edu.unbosque.reventacero.repository.DispositivoRepository;
import co.edu.unbosque.reventacero.repository.UsuarioRepository;
import co.edu.unbosque.reventacero.util.Validaciones;

@Service
public class DispositivoService {

	private final DispositivoRepository dispositivoRepo;

	private final UsuarioRepository usuarioRepo;

	private final ModelMapper modelMapper;

	public DispositivoService(DispositivoRepository dispositivoRepo, UsuarioRepository usuarioRepo,
			ModelMapper modelMapper) {
		this.dispositivoRepo = dispositivoRepo;
		this.usuarioRepo = usuarioRepo;
		this.modelMapper = modelMapper;
	}

	@Transactional
	public DispositivoDTO crear(DispositivoDTO dto) {

		validar(dto, 0);
		Validaciones.requerirPositivo(dto.getDocumentoUsuario(), "documentoUsuario");

		if (!usuarioRepo.existsByDocumento(dto.getDocumentoUsuario())) {
			throw new RecursoNoEncontradoException("El usuario " + dto.getDocumentoUsuario() + " no existe");
		}

		Usuario usuario = new Usuario();
		usuario.setDocumento(dto.getDocumentoUsuario());

		Dispositivo dispositivo = new Dispositivo();
		dispositivo.setIdentificadorUnico(dto.getIdentificadorUnico().trim());
		dispositivo.setTipo(dto.getTipo().trim());
		dispositivo.setActivo(true);
		dispositivo.setUsuario(usuario);

		try {
			dispositivoRepo.save(dispositivo);
		} catch (DataIntegrityViolationException e) {
			throw new ReglaNegocioException("No se pudo registrar el dispositivo");
		}

		return aDTO(dispositivo);
	}

	@Transactional
	public DispositivoDTO actualizar(long id, DispositivoDTO dto) {

		Dispositivo dispositivo = buscarEntidad(id);
		validar(dto, id);

		dispositivo.setIdentificadorUnico(dto.getIdentificadorUnico().trim());
		dispositivo.setTipo(dto.getTipo().trim());
		dispositivo.setActivo(dto.isActivo());
		dispositivoRepo.update(dispositivo);

		return aDTO(dispositivo);
	}

	@Transactional
	public DispositivoDTO cambiarEstado(long id, boolean activo) {

		Dispositivo dispositivo = buscarEntidad(id);

		dispositivo.setActivo(activo);
		dispositivoRepo.update(dispositivo);

		return aDTO(dispositivo);
	}

	@Transactional
	public void eliminar(long id) {

		buscarEntidad(id);

		try {
			dispositivoRepo.deleteById(id);
		} catch (DataIntegrityViolationException e) {
			throw new ReglaNegocioException(
					"No se puede eliminar el dispositivo porque tiene transacciones asociadas; desactívelo en su lugar");
		}
	}

	public DispositivoDTO buscarPorId(long id) {
		return aDTO(buscarEntidad(id));
	}

	public List<DispositivoDTO> listar() {
		return dispositivoRepo.findAll().stream().map(this::aDTO).toList();
	}

	public List<DispositivoDTO> listarPorUsuario(long documento) {
		if (!usuarioRepo.existsByDocumento(documento)) {
			throw new RecursoNoEncontradoException("El usuario " + documento + " no existe");
		}
		return dispositivoRepo.findByUsuarioDocumento(documento).stream().map(this::aDTO).toList();
	}

	private Dispositivo buscarEntidad(long id) {
		return dispositivoRepo.findById(id)
				.orElseThrow(() -> new RecursoNoEncontradoException("El dispositivo " + id + " no existe"));
	}

	private void validar(DispositivoDTO dto, long idPropio) {

		Validaciones.requerirTexto(dto.getIdentificadorUnico(), "identificadorUnico");
		Validaciones.requerirTexto(dto.getTipo(), "tipo");

		if (dto.getIdentificadorUnico().trim().length() > 254) {
			throw new IllegalArgumentException("El identificador no puede tener más de 254 caracteres");
		}

		if (dto.getTipo().trim().length() > 100) {
			throw new IllegalArgumentException("El tipo no puede tener más de 100 caracteres");
		}

		for (Dispositivo existente : dispositivoRepo.findByIdentificadorUnico(dto.getIdentificadorUnico().trim())) {
			if (existente.getIdDispositivo() != idPropio) {
				throw new ReglaNegocioException("Ya existe un dispositivo con ese identificador");
			}
		}
	}

	private DispositivoDTO aDTO(Dispositivo dispositivo) {
		DispositivoDTO dto = modelMapper.map(dispositivo, DispositivoDTO.class);
		dto.setDocumentoUsuario(dispositivo.getUsuario() != null ? dispositivo.getUsuario().getDocumento() : 0);
		return dto;
	}

	public Dispositivo validarDispositivoActivo(long id) {

		Dispositivo dispositivo = buscarEntidad(id);

		if (!dispositivo.isActivo()) {
			throw new ReglaNegocioException("El dispositivo " + id + " no está activo");
		}

		return dispositivo;
	}
}