package co.edu.unbosque.reventacero.controller;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import co.edu.unbosque.reventacero.dto.TransaccionDTO;
import co.edu.unbosque.reventacero.entity.Usuario;
import co.edu.unbosque.reventacero.enums.TipoUsuario;
import co.edu.unbosque.reventacero.service.BoletaService;
import co.edu.unbosque.reventacero.service.DispositivoService;
import co.edu.unbosque.reventacero.service.TransaccionService;

/** RF-14, RF-20; controlador de Transaccion de Fase 2, semana 12. */
@RestController
@RequestMapping("/transacciones")
public class TransaccionController {

    private final TransaccionService transaccionService;

    private final BoletaService boletaService;

    private final DispositivoService dispositivoService;

    public TransaccionController(TransaccionService transaccionService, BoletaService boletaService, DispositivoService dispositivoService) {
        this.transaccionService = transaccionService;
        this.boletaService = boletaService;
        this.dispositivoService = dispositivoService;
    }

    @PreAuthorize("hasRole('ADMINISTRADOR')")
    @GetMapping
    public List<TransaccionDTO> listar() {
        return transaccionService.listar();
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> buscarPorId(@PathVariable long id, @AuthenticationPrincipal Usuario actual) {
        TransaccionDTO dto = transaccionService.buscarPorId(id);
        if (!(puedeConsultarBoleta(dto.getCodigoBoleta(), actual))) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("No tienes permiso para consultar o modificar este recurso");
        }
        return ResponseEntity.ok(dto);
    }

    @GetMapping("/boleta/{codigoBoleta}")
    public ResponseEntity<?> listarPorBoleta(@PathVariable long codigoBoleta, @AuthenticationPrincipal Usuario actual) {
        if (!(puedeConsultarBoleta(codigoBoleta, actual))) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("No tienes permiso para consultar o modificar este recurso");
        }
        return ResponseEntity.ok(transaccionService.listarPorBoleta(codigoBoleta));
    }

    @GetMapping("/dispositivo/{idDispositivo}")
    public ResponseEntity<?> listarPorDispositivo(@PathVariable long idDispositivo, @AuthenticationPrincipal Usuario actual) {
        if (!(esAdministrador(actual) || dispositivoService.buscarPorId(idDispositivo).getDocumentoUsuario() == actual.getDocumento())) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("No tienes permiso para consultar o modificar este recurso");
        }
        return ResponseEntity.ok(transaccionService.listarPorDispositivo(idDispositivo));
    }

    private boolean esAdministrador(Usuario actual) {
        return actual != null && actual.getTipoUsuario() == TipoUsuario.ADMINISTRADOR;
    }

    private boolean puedeConsultarBoleta(long codigoBoleta, Usuario actual) {
        return esAdministrador(actual) || actual != null
            && boletaService.buscarPorCodigo(codigoBoleta).getDocumentoComprador() == actual.getDocumento();
    }
}
