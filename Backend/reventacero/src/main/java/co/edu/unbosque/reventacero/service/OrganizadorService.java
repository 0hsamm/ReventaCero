package co.edu.unbosque.reventacero.service;

import java.time.LocalDate;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import org.modelmapper.ModelMapper;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import co.edu.unbosque.reventacero.dto.OrganizadorDTO;
import co.edu.unbosque.reventacero.entity.Administrador;
import co.edu.unbosque.reventacero.entity.Organizador;
import co.edu.unbosque.reventacero.enums.TipoUsuario;
import co.edu.unbosque.reventacero.exception.RecursoNoEncontradoException;
import co.edu.unbosque.reventacero.exception.ReglaNegocioException;
import co.edu.unbosque.reventacero.repository.AdministradorRepository;
import co.edu.unbosque.reventacero.repository.FestivalRepository;
import co.edu.unbosque.reventacero.repository.OrganizadorRepository;
import co.edu.unbosque.reventacero.repository.UsuarioRepository;
import co.edu.unbosque.reventacero.util.Validaciones;

@Service
public class OrganizadorService {

	private final OrganizadorRepository organizadorRepo;

	private final UsuarioRepository usuarioRepo;

	private final AdministradorRepository administradorRepo;

	private final FestivalRepository festivalRepo;

	private final UsuarioService usuarioService;

	private final ModelMapper modelMapper;

	public OrganizadorService(OrganizadorRepository organizadorRepo, UsuarioRepository usuarioRepo,
			AdministradorRepository administradorRepo, FestivalRepository festivalRepo, UsuarioService usuarioService,
			ModelMapper modelMapper) {
		this.organizadorRepo = organizadorRepo;
		this.usuarioRepo = usuarioRepo;
		this.administradorRepo = administradorRepo;
		this.festivalRepo = festivalRepo;
		this.usuarioService = usuarioService;
		this.modelMapper = modelMapper;
	}

	@Transactional
	public OrganizadorDTO crear(OrganizadorDTO dto) {

		usuarioService.validarNuevo(dto);
		Validaciones.requerirTexto(dto.getCargo(), "cargo");
		Validaciones.requerirNoNulo(dto.getAutorizadoPorDocumento(), "autorizadoPorDocumento");

		if (!administradorRepo.existsById(dto.getAutorizadoPorDocumento())) {
			throw new RecursoNoEncontradoException(
					"El administrador " + dto.getAutorizadoPorDocumento() + " no existe");
		}

		Organizador organizador = new Organizador();
		usuarioService.prepararNuevo(organizador, dto, TipoUsuario.ORGANIZADOR);
		organizador.setCargo(dto.getCargo().trim());

		Administrador autorizadoPor = new Administrador();
		autorizadoPor.setDocumento(dto.getAutorizadoPorDocumento());
		organizador.setAutorizadoPor(autorizadoPor);
		organizador.setFechaAutorizacion(LocalDate.now());

		if (dto.getSupervisorDocumento() != null) {
			if (dto.getSupervisorDocumento() == dto.getDocumento()) {
				throw new ReglaNegocioException("Un organizador no puede ser su propio supervisor");
			}
			organizador.setSupervisor(buscarEntidad(dto.getSupervisorDocumento()));
		}

		try {
			usuarioRepo.save(organizador);
			organizadorRepo.save(organizador);
		} catch (DataIntegrityViolationException e) {
			throw new ReglaNegocioException("El documento, el nombre de usuario o el correo ya están registrados");
		}

		return aDTO(organizador);
	}

	@Transactional
	public OrganizadorDTO actualizar(long documento, OrganizadorDTO dto) {

		Organizador organizador = buscarEntidad(documento);

		Validaciones.requerirTexto(dto.getCargo(), "cargo");

		if (dto.getSupervisorDocumento() != null) {
			validarSupervisor(documento, dto.getSupervisorDocumento());
			organizador.setSupervisor(buscarEntidad(dto.getSupervisorDocumento()));
		} else {
			organizador.setSupervisor(null);
		}

		organizador.setCargo(dto.getCargo().trim());
		organizadorRepo.update(organizador);

		return aDTO(organizador);
	}

	@Transactional
	public void eliminar(long documento) {

		buscarEntidad(documento);

		if (!organizadorRepo.findBySupervisorDocumento(documento).isEmpty()) {
			throw new ReglaNegocioException("No se puede eliminar el organizador porque supervisa a otros organizadores");
		}

		if (!festivalRepo.findByOrganizadorDocumento(documento).isEmpty()) {
			throw new ReglaNegocioException("No se puede eliminar el organizador porque tiene festivales a su cargo");
		}

		try {
			organizadorRepo.deleteById(documento);
			usuarioRepo.deleteById(documento);
		} catch (DataIntegrityViolationException e) {
			throw new ReglaNegocioException("No se puede eliminar el organizador porque tiene registros asociados");
		}
	}

	public OrganizadorDTO buscarPorDocumento(long documento) {
		return aDTO(buscarEntidad(documento));
	}

	public List<OrganizadorDTO> listar() {
		return organizadorRepo.findAll().stream().map(this::aDTO).toList();
	}

	public List<OrganizadorDTO> listarSupervisados(long documentoSupervisor) {
		buscarEntidad(documentoSupervisor);
		return organizadorRepo.findBySupervisorDocumento(documentoSupervisor).stream().map(this::aDTO).toList();
	}

	private Organizador buscarEntidad(long documento) {
		return organizadorRepo.findById(documento)
				.orElseThrow(() -> new RecursoNoEncontradoException("El organizador " + documento + " no existe"));
	}

	private void validarSupervisor(long documento, long documentoSupervisor) {

		if (documentoSupervisor == documento) {
			throw new ReglaNegocioException("Un organizador no puede ser su propio supervisor");
		}

		Set<Long> visitados = new HashSet<>();
		Optional<Organizador> actual = organizadorRepo.findById(documentoSupervisor);

		if (actual.isEmpty()) {
			throw new RecursoNoEncontradoException("El organizador " + documentoSupervisor + " no existe");
		}

		while (actual.isPresent() && visitados.add(actual.get().getDocumento())) {
			Organizador siguiente = actual.get().getSupervisor();
			if (siguiente == null) {
				return;
			}
			if (siguiente.getDocumento() == documento) {
				throw new ReglaNegocioException("El supervisor indicado genera un ciclo en la jerarquía");
			}
			actual = organizadorRepo.findById(siguiente.getDocumento());
		}
	}

	private OrganizadorDTO aDTO(Organizador organizador) {
		OrganizadorDTO dto = modelMapper.map(organizador, OrganizadorDTO.class);
		usuarioService.completarDTO(organizador, dto);
		dto.setSupervisorDocumento(organizador.getSupervisor() != null ? organizador.getSupervisor().getDocumento() : null);
		return dto;
	}

}