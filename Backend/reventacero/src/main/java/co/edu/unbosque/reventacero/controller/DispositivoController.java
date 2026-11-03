package co.edu.unbosque.reventacero.controller;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import co.edu.unbosque.reventacero.dto.DispositivoDTO;
import co.edu.unbosque.reventacero.entity.Usuario;
import co.edu.unbosque.reventacero.enums.TipoUsuario;
import co.edu.unbosque.reventacero.service.DispositivoService;

/** RF-11; controlador de Dispositivo de Fase 2, semana 12. */
@RestController
@RequestMapping("/dispositivos")
public class DispositivoController {

    private final DispositivoService dispositivoService;

    public DispositivoController(DispositivoService dispositivoService) {
        this.dispositivoService = dispositivoService;
    }

    @PreAuthorize("hasRole('ADMINISTRADOR')")
    @GetMapping
    public List<DispositivoDTO> listar() {
        return dispositivoService.listar();
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> buscarPorId(@PathVariable long id, @AuthenticationPrincipal Usuario actual) {
        DispositivoDTO dto = dispositivoService.buscarPorId(id);
        if (!(puedeAcceder(dto.getDocumentoUsuario(), actual))) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("No tienes permiso para consultar o modificar este recurso");
        }
        return ResponseEntity.ok(dto);
    }

    @GetMapping("/usuario/{documento}")
    public ResponseEntity<?> listarPorUsuario(@PathVariable long documento, @AuthenticationPrincipal Usuario actual) {
        if (!(puedeAcceder(documento, actual))) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("No tienes permiso para consultar o modificar este recurso");
        }
        return ResponseEntity.ok(dispositivoService.listarPorUsuario(documento));
    }

    @PostMapping
    public ResponseEntity<DispositivoDTO> crear(@RequestBody DispositivoDTO dto, @AuthenticationPrincipal Usuario actual) {
        if (!esAdministrador(actual)) {
            dto.setDocumentoUsuario(actual.getDocumento());
        }
        return ResponseEntity.status(HttpStatus.CREATED).body(dispositivoService.crear(dto));
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> actualizar(@PathVariable long id, @RequestBody DispositivoDTO dto, @AuthenticationPrincipal Usuario actual) {
        DispositivoDTO existente = dispositivoService.buscarPorId(id);
        if (!(puedeAcceder(existente.getDocumentoUsuario(), actual))) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("No tienes permiso para consultar o modificar este recurso");
        }
        if (!esAdministrador(actual)) {
            dto.setDocumentoUsuario(actual.getDocumento());
        }
        return ResponseEntity.ok(dispositivoService.actualizar(id, dto));
    }

    @PutMapping("/{id}/estado")
    public ResponseEntity<?> cambiarEstado(@PathVariable long id, @RequestBody CambioEstado solicitud, @AuthenticationPrincipal Usuario actual) {
        if (solicitud.activo() == null) {
            return ResponseEntity.badRequest().body("El campo activo es obligatorio");
        }
        if (!(puedeAcceder(dispositivoService.buscarPorId(id).getDocumentoUsuario(), actual))) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("No tienes permiso para consultar o modificar este recurso");
        }
        return ResponseEntity.ok(dispositivoService.cambiarEstado(id, solicitud.activo()));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> eliminar(@PathVariable long id, @AuthenticationPrincipal Usuario actual) {
        if (!(puedeAcceder(dispositivoService.buscarPorId(id).getDocumentoUsuario(), actual))) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("No tienes permiso para consultar o modificar este recurso");
        }
        dispositivoService.eliminar(id);
        return ResponseEntity.noContent().build();
    }

    public record CambioEstado(Boolean activo) {
    }

    private boolean esAdministrador(Usuario actual) {
        return actual != null && actual.getTipoUsuario() == TipoUsuario.ADMINISTRADOR;
    }

    private boolean puedeAcceder(long documento, Usuario actual) {
        return esAdministrador(actual) || actual != null && actual.getDocumento() == documento;
    }
}
