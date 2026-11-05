package co.edu.unbosque.reventacero.controller;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import co.edu.unbosque.reventacero.dto.SolicitudReventaDTO;
import co.edu.unbosque.reventacero.entity.Usuario;
import co.edu.unbosque.reventacero.service.BoletaService;
import co.edu.unbosque.reventacero.service.SolicitudReventaService;

/** Aceptar compromete a la propia cuenta: ni un administrador responde por otra persona. */
@RestController
@RequestMapping("/solicitudes-reventa")
public class SolicitudReventaController {

    private final SolicitudReventaService solicitudService;

    private final BoletaService boletaService;

    public SolicitudReventaController(SolicitudReventaService solicitudService, BoletaService boletaService) {
        this.solicitudService = solicitudService;
        this.boletaService = boletaService;
    }

    @GetMapping("/recibidas")
    public List<SolicitudReventaDTO> recibidas(@AuthenticationPrincipal Usuario actual) {
        return solicitudService.recibidas(actual.getDocumento());
    }

    @GetMapping("/enviadas")
    public List<SolicitudReventaDTO> enviadas(@AuthenticationPrincipal Usuario actual) {
        return solicitudService.enviadas(actual.getDocumento());
    }

    @PostMapping
    public ResponseEntity<?> crear(@RequestBody SolicitudReventaDTO dto, @AuthenticationPrincipal Usuario actual) {
        if (boletaService.buscarPorCodigo(dto.getCodigoBoleta()).getDocumentoComprador() != actual.getDocumento()) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("La boleta no pertenece a tu cuenta");
        }
        return ResponseEntity.status(HttpStatus.CREATED).body(solicitudService.crear(dto, actual.getDocumento()));
    }

    @PutMapping("/{id}/aceptar")
    public ResponseEntity<?> aceptar(@PathVariable long id, @RequestBody Aceptacion aceptacion, @AuthenticationPrincipal Usuario actual) {
        if (!esReceptor(id, actual)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(SOLO_RECEPTOR);
        }
        return ResponseEntity.ok(solicitudService.aceptar(id, aceptacion.idDispositivo()));
    }

    @PutMapping("/{id}/rechazar")
    public ResponseEntity<?> rechazar(@PathVariable long id, @AuthenticationPrincipal Usuario actual) {
        if (!esReceptor(id, actual)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(SOLO_RECEPTOR);
        }
        return ResponseEntity.ok(solicitudService.rechazar(id));
    }

    @PutMapping("/{id}/cancelar")
    public ResponseEntity<?> cancelar(@PathVariable long id, @AuthenticationPrincipal Usuario actual) {
        if (solicitudService.buscarPorId(id).getVendedorDocumento() != actual.getDocumento()) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Solo quien hizo la oferta puede cancelarla");
        }
        return ResponseEntity.ok(solicitudService.cancelar(id));
    }

    public record Aceptacion(long idDispositivo) {
    }

    private static final String SOLO_RECEPTOR = "Solo quien recibió la oferta puede responderla";

    private boolean esReceptor(long id, Usuario actual) {
        return solicitudService.buscarPorId(id).getCompradorDocumento() == actual.getDocumento();
    }
}
