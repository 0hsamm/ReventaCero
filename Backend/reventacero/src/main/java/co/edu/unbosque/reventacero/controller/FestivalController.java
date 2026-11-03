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

import co.edu.unbosque.reventacero.dto.FestivalDTO;
import co.edu.unbosque.reventacero.entity.Usuario;
import co.edu.unbosque.reventacero.enums.TipoUsuario;
import co.edu.unbosque.reventacero.service.FestivalService;

/** Controlador de Festival. RF-01; Fase 2, semana 12. */
@RestController
@RequestMapping("/festivales")
public class FestivalController {

    private final FestivalService festivalService;

    public FestivalController(FestivalService festivalService) {
        this.festivalService = festivalService;
    }

    @GetMapping
    public List<FestivalDTO> listar() {
        return festivalService.listar();
    }

    @GetMapping("/{id}")
    public FestivalDTO buscarPorId(@PathVariable long id) {
        return festivalService.buscarPorId(id);
    }

    @GetMapping("/organizador/{documento}")
    public List<FestivalDTO> listarPorOrganizador(@PathVariable long documento) {
        return festivalService.listarPorOrganizador(documento);
    }

    @PreAuthorize("hasAnyRole('ADMINISTRADOR', 'ORGANIZADOR')")
    @PostMapping
    public ResponseEntity<FestivalDTO> crear(@RequestBody FestivalDTO dto, @AuthenticationPrincipal Usuario actual) {
        if (actual.getTipoUsuario() == TipoUsuario.ORGANIZADOR) {
            dto.setDocumentoOrganizador(actual.getDocumento());
        }
        return ResponseEntity.status(HttpStatus.CREATED).body(festivalService.crear(dto));
    }

    @PreAuthorize("hasAnyRole('ADMINISTRADOR', 'ORGANIZADOR')")
    @PutMapping("/{id}")
    public ResponseEntity<?> actualizar(@PathVariable long id, @RequestBody FestivalDTO dto, @AuthenticationPrincipal Usuario actual) {
        if (!(puedeGestionar(id, actual))) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("No eres el organizador de este festival");
        }
        if (actual.getTipoUsuario() == TipoUsuario.ORGANIZADOR) {
            dto.setDocumentoOrganizador(actual.getDocumento());
        }
        return ResponseEntity.ok(festivalService.actualizar(id, dto));
    }

    @PreAuthorize("hasAnyRole('ADMINISTRADOR', 'ORGANIZADOR')")
    @DeleteMapping("/{id}")
    public ResponseEntity<?> eliminar(@PathVariable long id, @AuthenticationPrincipal Usuario actual) {
        if (!(puedeGestionar(id, actual))) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("No eres el organizador de este festival");
        }
        festivalService.eliminar(id);
        return ResponseEntity.noContent().build();
    }

    private boolean esAdministrador(Usuario actual) {
        return actual != null && actual.getTipoUsuario() == TipoUsuario.ADMINISTRADOR;
    }

    private boolean puedeGestionar(long idFestival, Usuario actual) {
        if (esAdministrador(actual)) {
            return true;
        }
        return actual != null && actual.getTipoUsuario() == TipoUsuario.ORGANIZADOR
            && festivalService.buscarPorId(idFestival).getDocumentoOrganizador() == actual.getDocumento();
    }
}
