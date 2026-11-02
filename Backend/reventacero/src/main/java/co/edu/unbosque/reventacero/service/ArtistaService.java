package co.edu.unbosque.reventacero.service;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import org.modelmapper.ModelMapper;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import co.edu.unbosque.reventacero.dto.ArtistaDTO;
import co.edu.unbosque.reventacero.entity.Artista;
import co.edu.unbosque.reventacero.entity.Genero;
import co.edu.unbosque.reventacero.exception.RecursoNoEncontradoException;
import co.edu.unbosque.reventacero.exception.ReglaNegocioException;
import co.edu.unbosque.reventacero.repository.ArtistaRepository;
import co.edu.unbosque.reventacero.repository.GeneroRepository;
import co.edu.unbosque.reventacero.repository.PresentacionRepository;
import co.edu.unbosque.reventacero.util.Validaciones;


@Service
public class ArtistaService {

	private final ArtistaRepository artistaRepo;

	private final GeneroRepository generoRepo;

	private final PresentacionRepository presentacionRepo;

	private final ModelMapper modelMapper;

	public ArtistaService(ArtistaRepository artistaRepo, GeneroRepository generoRepo,
			PresentacionRepository presentacionRepo, ModelMapper modelMapper) {
		this.artistaRepo = artistaRepo;
		this.generoRepo = generoRepo;
		this.presentacionRepo = presentacionRepo;
		this.modelMapper = modelMapper;
	}

	@Transactional
	public ArtistaDTO crear(ArtistaDTO dto) {

		validar(dto, 0);

		Artista artista = aEntidad(dto);
		artistaRepo.save(artista);
		guardarRelaciones(artista.getIdArtista(), dto);

		return buscarPorId(artista.getIdArtista());
	}

	@Transactional
	public ArtistaDTO actualizar(long id, ArtistaDTO dto) {

		buscarEntidad(id);
		validar(dto, id);

		dto.setIdArtista(id);
		artistaRepo.update(aEntidad(dto));

		artistaRepo.eliminarRedesSociales(id);
		artistaRepo.eliminarInvitados(id);
		guardarRelaciones(id, dto);

		return buscarPorId(id);
	}

	@Transactional
	public void eliminar(long id) {

		buscarEntidad(id);

		if (!presentacionRepo.findByArtistaId(id).isEmpty()) {
			throw new ReglaNegocioException("No se puede eliminar el artista porque tiene presentaciones programadas");
		}

		try {
			artistaRepo.eliminarRedesSociales(id);
			artistaRepo.eliminarColaboracionesDe(id);
			artistaRepo.deleteById(id);
		} catch (DataIntegrityViolationException e) {
			throw new ReglaNegocioException("No se puede eliminar el artista porque tiene registros asociados");
		}
	}

	public ArtistaDTO buscarPorId(long id) {
		return aDTO(buscarEntidad(id));
	}

	public List<ArtistaDTO> listar() {
		return artistaRepo.findAll().stream().map(this::aDTO).toList();
	}

	public List<ArtistaDTO> listarPorGenero(long idGenero) {
		return artistaRepo.findByGeneroId(idGenero).stream().map(this::aDTO).toList();
	}

	public List<ArtistaDTO> buscarPorNombreArtistico(String nombreArtistico) {
		return artistaRepo.findByNombreArtistico(nombreArtistico).stream().map(this::aDTO).toList();
	}

	private Artista buscarEntidad(long id) {
		return artistaRepo.findById(id)
				.orElseThrow(() -> new RecursoNoEncontradoException("El artista " + id + " no existe"));
	}

	private void validar(ArtistaDTO dto, long idPropio) {

		Validaciones.requerirTexto(dto.getNombreArtistico(), "nombreArtistico");

		for (Artista existente : artistaRepo.findByNombreArtistico(dto.getNombreArtistico().trim())) {
			if (existente.getIdArtista() != idPropio) {
				throw new ReglaNegocioException("Ya existe un artista con el nombre " + dto.getNombreArtistico().trim());
			}
		}

		if (!generoRepo.existsById(dto.getIdGenero())) {
			throw new RecursoNoEncontradoException("El género " + dto.getIdGenero() + " no existe");
		}

		if (dto.getRedesSociales() != null) {
			for (String red : dto.getRedesSociales()) {
				Validaciones.requerirTexto(red, "redesSociales");
			}
		}

		if (dto.getIdArtistasInvitados() != null) {
			for (Long idInvitado : dto.getIdArtistasInvitados()) {

				if (idInvitado == null || !artistaRepo.existsById(idInvitado)) {
					throw new RecursoNoEncontradoException("El artista invitado " + idInvitado + " no existe");
				}

				if (idInvitado == idPropio) {
					throw new ReglaNegocioException("Un artista no puede colaborar consigo mismo");
				}
			}
		}
	}

	private void guardarRelaciones(long idArtista, ArtistaDTO dto) {

		if (dto.getRedesSociales() != null) {
			Set<String> redes = new LinkedHashSet<>();
			dto.getRedesSociales().forEach(red -> redes.add(red.trim()));
			redes.forEach(red -> artistaRepo.guardarRedSocial(idArtista, red));
		}

		if (dto.getIdArtistasInvitados() != null) {
			Set<Long> invitados = new LinkedHashSet<>(dto.getIdArtistasInvitados());
			invitados.forEach(idInvitado -> artistaRepo.guardarColaboracion(idArtista, idInvitado));
		}
	}

	private ArtistaDTO aDTO(Artista artista) {
		ArtistaDTO dto = modelMapper.map(artista, ArtistaDTO.class);
		dto.setIdGenero(artista.getGenero() != null ? artista.getGenero().getIdGenero() : 0);
		dto.setRedesSociales(new ArrayList<>(artista.getRedesSociales()));
		dto.setIdArtistasInvitados(
				artista.getArtistaInvitados().stream().map(Artista::getIdArtista).toList());
		return dto;
	}

	private Artista aEntidad(ArtistaDTO dto) {
		Artista artista = modelMapper.map(dto, Artista.class);
		artista.setNombreArtistico(dto.getNombreArtistico().trim());

		Genero genero = new Genero();
		genero.setIdGenero(dto.getIdGenero());
		artista.setGenero(genero);

		return artista;
	}

}