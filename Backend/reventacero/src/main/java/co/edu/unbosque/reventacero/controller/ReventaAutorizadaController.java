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

import co.edu.unbosque.reventacero.dto.ReventaAutorizadaDTO;
import co.edu.unbosque.reventacero.entity.Usuario;
import co.edu.unbosque.reventacero.enums.TipoUsuario;
import co.edu.unbosque.reventacero.service.BoletaService;
import co.edu.unbosque.reventacero.service.FestivalService;
import co.edu.unbosque.reventacero.service.ReventaAutorizadaService;

/**
 * RF-15, RF-21; controlador de ReventaAutorizada de Fase 2, semana 12. Solo consulta: una reventa nace
 * cuando alguien compra una boleta publicada en PublicacionReventaController.
 */
@RestController
@RequestMapping("/reventas-autorizadas")
public class ReventaAutorizadaController {

    private final ReventaAutorizadaService reventaAutorizadaService;

    private final BoletaService boletaService;

    private final FestivalService festivalService;

    public ReventaAutorizadaController(ReventaAutorizadaService reventaAutorizadaService, BoletaService boletaService, FestivalService festivalService) {
        this.reventaAutorizadaService = reventaAutorizadaService;
        this.boletaService = boletaService;
        this.festivalService = festivalService;
    }

    @PreAuthorize("hasRole('ADMINISTRADOR')")
    @GetMapping
    public List<ReventaAutorizadaDTO> listar() {
        return reventaAutorizadaService.listar();
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> buscarPorId(@PathVariable long id, @AuthenticationPrincipal Usuario actual) {
        ReventaAutorizadaDTO dto = reventaAutorizadaService.buscarPorId(id);
        if (!(puedeAcceder(dto.getVendedorDocumento(), actual) || puedeAcceder(dto.getCompradorDocumento(), actual))) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("No tienes permiso para consultar o modificar este recurso");
        }
        return ResponseEntity.ok(dto);
    }

    @GetMapping("/boleta/{codigoBoleta}")
    public ResponseEntity<?> listarPorBoleta(@PathVariable long codigoBoleta, @AuthenticationPrincipal Usuario actual) {
        if (!(puedeConsultarBoleta(codigoBoleta, actual))) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("No tienes permiso para consultar o modificar este recurso");
        }
        return ResponseEntity.ok(reventaAutorizadaService.listarPorBoleta(codigoBoleta));
    }

    @GetMapping("/vendedor/{documento}")
    public ResponseEntity<?> listarPorVendedor(@PathVariable long documento, @AuthenticationPrincipal Usuario actual) {
        if (!(puedeAcceder(documento, actual))) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("No tienes permiso para consultar o modificar este recurso");
        }
        return ResponseEntity.ok(reventaAutorizadaService.listarPorVendedor(documento));
    }

    @GetMapping("/comprador/{documento}")
    public ResponseEntity<?> listarPorComprador(@PathVariable long documento, @AuthenticationPrincipal Usuario actual) {
        if (!(puedeAcceder(documento, actual))) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("No tienes permiso para consultar o modificar este recurso");
        }
        return ResponseEntity.ok(reventaAutorizadaService.listarPorComprador(documento));
    }

    @PreAuthorize("hasAnyRole('ADMINISTRADOR', 'ORGANIZADOR')")
    @GetMapping("/festival/{idFestival}")
    public ResponseEntity<?> listarPorFestival(@PathVariable long idFestival, @AuthenticationPrincipal Usuario actual) {
        if (!(puedeGestionar(idFestival, actual))) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("No eres el organizador de este festival");
        }
        return ResponseEntity.ok(reventaAutorizadaService.listarPorFestival(idFestival));
    }

    private boolean esAdministrador(Usuario actual) {
        return actual != null && actual.getTipoUsuario() == TipoUsuario.ADMINISTRADOR;
    }

    private boolean puedeAcceder(long documento, Usuario actual) {
        return esAdministrador(actual) || actual != null && actual.getDocumento() == documento;
    }

    private boolean puedeConsultarBoleta(long codigoBoleta, Usuario actual) {
        return esAdministrador(actual) || actual != null
            && boletaService.buscarPorCodigo(codigoBoleta).getDocumentoComprador() == actual.getDocumento();
    }

    private boolean puedeGestionar(long idFestival, Usuario actual) {
        if (esAdministrador(actual)) {
            return true;
        }
        return actual != null && actual.getTipoUsuario() == TipoUsuario.ORGANIZADOR
            && festivalService.buscarPorId(idFestival).getDocumentoOrganizador() == actual.getDocumento();
    }
}
