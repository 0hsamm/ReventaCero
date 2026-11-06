package co.edu.unbosque.reventacero.service;

import java.util.List;

import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;

import co.edu.unbosque.reventacero.dto.TransaccionDTO;
import co.edu.unbosque.reventacero.entity.Transaccion;
import co.edu.unbosque.reventacero.exception.RecursoNoEncontradoException;
import co.edu.unbosque.reventacero.repository.BoletaRepository;
import co.edu.unbosque.reventacero.repository.DispositivoRepository;
import co.edu.unbosque.reventacero.repository.TransaccionRepository;

@Service
public class TransaccionService {

	private final TransaccionRepository transaccionRepo;

	private final BoletaRepository boletaRepo;

	private final DispositivoRepository dispositivoRepo;

	private final ModelMapper modelMapper;

	public TransaccionService(TransaccionRepository transaccionRepo, BoletaRepository boletaRepo,
			DispositivoRepository dispositivoRepo, ModelMapper modelMapper) {
		this.transaccionRepo = transaccionRepo;
		this.boletaRepo = boletaRepo;
		this.dispositivoRepo = dispositivoRepo;
		this.modelMapper = modelMapper;
	}

	public TransaccionDTO buscarPorId(long id) {
		return aDTO(transaccionRepo.findById(id)
				.orElseThrow(() -> new RecursoNoEncontradoException("La transacción " + id + " no existe")));
	}

	public List<TransaccionDTO> listar() {
		return transaccionRepo.findAll().stream().map(this::aDTO).toList();
	}

	public List<TransaccionDTO> listarPorBoleta(long codigoBoleta) {
		if (!boletaRepo.existsById(codigoBoleta)) {
			throw new RecursoNoEncontradoException("La boleta " + codigoBoleta + " no existe");
		}
		return transaccionRepo.findByBoletaCodigoBoleta(codigoBoleta).stream().map(this::aDTO).toList();
	}

	public List<TransaccionDTO> listarPorDispositivo(long idDispositivo) {
		if (!dispositivoRepo.existsById(idDispositivo)) {
			throw new RecursoNoEncontradoException("El dispositivo " + idDispositivo + " no existe");
		}
		return transaccionRepo.findByDispositivoId(idDispositivo).stream().map(this::aDTO).toList();
	}

	public TransaccionDTO aDTO(Transaccion transaccion) {
		TransaccionDTO dto = modelMapper.map(transaccion, TransaccionDTO.class);
		completarDTO(transaccion, dto);
		return dto;
	}

	public void completarDTO(Transaccion transaccion, TransaccionDTO dto) {
		dto.setCodigoBoleta(transaccion.getBoleta() != null ? transaccion.getBoleta().getCodigoBoleta() : 0);
		dto.setIdDispositivo(transaccion.getDispositivo() != null ? transaccion.getDispositivo().getIdDispositivo() : 0);
		dto.setTipoTransaccion(
				transaccion.getTipoTransaccion() != null ? transaccion.getTipoTransaccion().name() : null);
	}

}