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
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import co.edu.unbosque.reventacero.dto.InscripcionDTO;
import co.edu.unbosque.reventacero.entity.Usuario;
import co.edu.unbosque.reventacero.enums.TipoUsuario;
import co.edu.unbosque.reventacero.service.InscripcionService;

/** RF-16; controlador de Inscripcion de Fase 2, semana 12. */
@RestController
@RequestMapping("/inscripciones")
public class InscripcionController {

    private final InscripcionService inscripcionService;

    public InscripcionController(InscripcionService inscripcionService) {
        this.inscripcionService = inscripcionService;
    }

    @PreAuthorize("hasRole('ADMINISTRADOR')")
    @GetMapping
    public List<InscripcionDTO> listar() {
        return inscripcionService.listar();
    }

    @GetMapping("/{documento}/{idLista}")
    public ResponseEntity<?> buscar(@PathVariable long documento, @PathVariable long idLista, @AuthenticationPrincipal Usuario actual) {
        if (!(puedeAcceder(documento, actual))) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("No tienes permiso para consultar o modificar este recurso");
        }
        return ResponseEntity.ok(inscripcionService.buscar(documento, idLista));
    }

    @GetMapping("/{documento}/{idLista}/posicion")
    public ResponseEntity<?> posicionEnCola(@PathVariable long documento, @PathVariable long idLista, @AuthenticationPrincipal Usuario actual) {
        if (!(puedeAcceder(documento, actual))) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("No tienes permiso para consultar o modificar este recurso");
        }
        return ResponseEntity.ok(inscripcionService.posicionEnCola(documento, idLista));
    }

    @GetMapping("/usuario/{documento}")
    public ResponseEntity<?> listarPorUsuario(@PathVariable long documento, @AuthenticationPrincipal Usuario actual) {
        if (!(puedeAcceder(documento, actual))) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("No tienes permiso para consultar o modificar este recurso");
        }
        return ResponseEntity.ok(inscripcionService.listarPorUsuario(documento));
    }

    @PreAuthorize("hasRole('ADMINISTRADOR')")
    @GetMapping("/lista/{idLista}")
    public List<InscripcionDTO> listarPorLista(@PathVariable long idLista) {
        return inscripcionService.listarPorLista(idLista);
    }

    @PostMapping
    public ResponseEntity<InscripcionDTO> inscribir(@RequestBody InscripcionDTO dto, @AuthenticationPrincipal Usuario actual) {
        if (!esAdministrador(actual)) {
            dto.setDocumento(actual.getDocumento());
        }
        return ResponseEntity.status(HttpStatus.CREATED).body(inscripcionService.inscribir(dto));
    }

    @DeleteMapping("/{documento}/{idLista}")
    public ResponseEntity<?> cancelar(@PathVariable long documento, @PathVariable long idLista, @AuthenticationPrincipal Usuario actual) {
        if (!(puedeAcceder(documento, actual))) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("No tienes permiso para consultar o modificar este recurso");
        }
        inscripcionService.cancelar(documento, idLista);
        return ResponseEntity.noContent().build();
    }

    private boolean esAdministrador(Usuario actual) {
        return actual != null && actual.getTipoUsuario() == TipoUsuario.ADMINISTRADOR;
    }

    private boolean puedeAcceder(long documento, Usuario actual) {
        return esAdministrador(actual) || actual != null && actual.getDocumento() == documento;
    }
}
