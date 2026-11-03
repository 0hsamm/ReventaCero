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

import co.edu.unbosque.reventacero.dto.EscenarioDTO;
import co.edu.unbosque.reventacero.entity.Usuario;
import co.edu.unbosque.reventacero.enums.TipoUsuario;
import co.edu.unbosque.reventacero.service.EscenarioService;
import co.edu.unbosque.reventacero.service.FestivalService;

/** Controlador de Escenario. RF-03, RF-18; Fase 2, semana 12. */
@RestController
@RequestMapping("/escenarios")
public class EscenarioController {

    private final EscenarioService escenarioService;

    private final FestivalService festivalService;

    public EscenarioController(EscenarioService escenarioService, FestivalService festivalService) {
        this.escenarioService = escenarioService;
        this.festivalService = festivalService;
    }

    @GetMapping
    public List<EscenarioDTO> listar() {
        return escenarioService.listar();
    }

    @GetMapping("/{idFestival}/{nombreEscenario}")
    public EscenarioDTO buscar(@PathVariable long idFestival, @PathVariable String nombreEscenario) {
        return escenarioService.buscar(idFestival, nombreEscenario);
    }

    @GetMapping("/festival/{idFestival}")
    public List<EscenarioDTO> listarPorFestival(@PathVariable long idFestival) {
        return escenarioService.listarPorFestival(idFestival);
    }

    @PreAuthorize("hasAnyRole('ADMINISTRADOR', 'ORGANIZADOR')")
    @PostMapping
    public ResponseEntity<?> crear(@RequestBody EscenarioDTO dto, @AuthenticationPrincipal Usuario actual) {
        if (!(puedeGestionar(dto.getIdFestival(), actual))) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("No eres el organizador de este festival");
        }
        return ResponseEntity.status(HttpStatus.CREATED).body(escenarioService.crear(dto));
    }

    @PreAuthorize("hasAnyRole('ADMINISTRADOR', 'ORGANIZADOR')")
    @PutMapping("/{idFestival}/{nombreEscenario}")
    public ResponseEntity<?> actualizar(@PathVariable long idFestival, @PathVariable String nombreEscenario, @RequestBody EscenarioDTO dto, @AuthenticationPrincipal Usuario actual) {
        if (!(puedeGestionar(idFestival, actual))) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("No eres el organizador de este festival");
        }
        return ResponseEntity.ok(escenarioService.actualizar(idFestival, nombreEscenario, dto));
    }

    @PreAuthorize("hasAnyRole('ADMINISTRADOR', 'ORGANIZADOR')")
    @DeleteMapping("/{idFestival}/{nombreEscenario}")
    public ResponseEntity<?> eliminar(@PathVariable long idFestival, @PathVariable String nombreEscenario, @AuthenticationPrincipal Usuario actual) {
        if (!(puedeGestionar(idFestival, actual))) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("No eres el organizador de este festival");
        }
        escenarioService.eliminar(idFestival, nombreEscenario);
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
