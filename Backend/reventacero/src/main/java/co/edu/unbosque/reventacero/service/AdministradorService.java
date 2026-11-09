package co.edu.unbosque.reventacero.service;

import java.time.LocalDate;
import java.util.List;

import org.modelmapper.ModelMapper;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import co.edu.unbosque.reventacero.dto.AdministradorDTO;
import co.edu.unbosque.reventacero.entity.Administrador;
import co.edu.unbosque.reventacero.enums.TipoUsuario;
import co.edu.unbosque.reventacero.exception.RecursoNoEncontradoException;
import co.edu.unbosque.reventacero.exception.ReglaNegocioException;
import co.edu.unbosque.reventacero.repository.AdministradorRepository;
import co.edu.unbosque.reventacero.repository.UsuarioRepository;
import co.edu.unbosque.reventacero.util.Validaciones;

@Service
public class AdministradorService {

	private final AdministradorRepository administradorRepo;

	private final UsuarioRepository usuarioRepo;

	private final UsuarioService usuarioService;

	private final ModelMapper modelMapper;

	public AdministradorService(AdministradorRepository administradorRepo, UsuarioRepository usuarioRepo,
			UsuarioService usuarioService, ModelMapper modelMapper) {
		this.administradorRepo = administradorRepo;
		this.usuarioRepo = usuarioRepo;
		this.usuarioService = usuarioService;
		this.modelMapper = modelMapper;
	}

	@Transactional
	public AdministradorDTO crear(AdministradorDTO dto) {

		usuarioService.validarNuevo(dto);
		Validaciones.requerirTexto(dto.getNivelAcceso(), "nivelAcceso");

		Administrador administrador = new Administrador();
		usuarioService.prepararNuevo(administrador, dto, TipoUsuario.ADMINISTRADOR);
		administrador.setNivelAcceso(dto.getNivelAcceso().trim());
		administrador.setFechaDesignacion(LocalDate.now());

		if (administradorRepo.count() > 0) {
			Validaciones.requerirNoNulo(dto.getAutorizadoPorDocumento(), "autorizadoPorDocumento");
		}

		if (dto.getAutorizadoPorDocumento() != null) {
			if (!administradorRepo.existsById(dto.getAutorizadoPorDocumento())) {
				throw new RecursoNoEncontradoException(
						"El administrador " + dto.getAutorizadoPorDocumento() + " no existe");
			}
			Administrador autorizadoPor = new Administrador();
			autorizadoPor.setDocumento(dto.getAutorizadoPorDocumento());
			administrador.setAutorizadoPor(autorizadoPor);
			administrador.setFechaAutorizacion(LocalDate.now());
		}

		try {
			usuarioRepo.save(administrador);
			administradorRepo.save(administrador);
		} catch (DataIntegrityViolationException e) {
			throw new ReglaNegocioException("El documento, el nombre de usuario o el correo ya están registrados");
		}

		return aDTO(administrador);
	}

	@Transactional
	public AdministradorDTO actualizar(long documento, AdministradorDTO dto) {

		Administrador administrador = buscarEntidad(documento);

		Validaciones.requerirTexto(dto.getNivelAcceso(), "nivelAcceso");

		administrador.setNivelAcceso(dto.getNivelAcceso().trim());
		administradorRepo.update(administrador);

		return aDTO(administrador);
	}

	@Transactional
	public void eliminar(long documento) {

		buscarEntidad(documento);

		if (administradorRepo.count() <= 1) {
			throw new ReglaNegocioException("No se puede eliminar al único administrador del sistema");
		}

		if (usuarioRepo.countByAutorizadoPor(documento) > 0) {
			throw new ReglaNegocioException("No se puede eliminar el administrador porque autorizó a otros usuarios");
		}

		try {
			administradorRepo.deleteById(documento);
			usuarioRepo.deleteById(documento);
		} catch (DataIntegrityViolationException e) {
			throw new ReglaNegocioException("No se puede eliminar el administrador porque tiene registros asociados");
		}
	}

	public AdministradorDTO buscarPorDocumento(long documento) {
		return aDTO(buscarEntidad(documento));
	}

	public List<AdministradorDTO> listar() {
		return administradorRepo.findAll().stream().map(this::aDTO).toList();
	}

	private Administrador buscarEntidad(long documento) {
		return administradorRepo.findById(documento)
				.orElseThrow(() -> new RecursoNoEncontradoException("El administrador " + documento + " no existe"));
	}

	private AdministradorDTO aDTO(Administrador administrador) {
		AdministradorDTO dto = modelMapper.map(administrador, AdministradorDTO.class);
		usuarioService.completarDTO(administrador, dto);
		return dto;
	}

}