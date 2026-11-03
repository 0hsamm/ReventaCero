package co.edu.unbosque.reventacero.controller;

import java.math.BigDecimal;
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

import co.edu.unbosque.reventacero.dto.PatrocinioDTO;
import co.edu.unbosque.reventacero.entity.Usuario;
import co.edu.unbosque.reventacero.enums.TipoUsuario;
import co.edu.unbosque.reventacero.service.FestivalService;
import co.edu.unbosque.reventacero.service.PatrocinioService;

/** Controlador de Patrocinio. RF-08, RF-24; Fase 2, semana 12. */
@RestController
@RequestMapping("/patrocinios")
public class PatrocinioController {

    private final PatrocinioService patrocinioService;

    private final FestivalService festivalService;

    public PatrocinioController(PatrocinioService patrocinioService, FestivalService festivalService) {
        this.patrocinioService = patrocinioService;
        this.festivalService = festivalService;
    }

    @GetMapping
    public List<PatrocinioDTO> listar() {
        return patrocinioService.listar();
    }

    @GetMapping("/{id}")
    public PatrocinioDTO buscarPorId(@PathVariable long id) {
        return patrocinioService.buscarPorId(id);
    }

    @GetMapping("/festival/{idFestival}")
    public List<PatrocinioDTO> listarPorFestival(@PathVariable long idFestival) {
        return patrocinioService.listarPorFestival(idFestival);
    }

    @GetMapping("/patrocinador/{idPatrocinador}")
    public List<PatrocinioDTO> listarPorPatrocinador(@PathVariable long idPatrocinador) {
        return patrocinioService.listarPorPatrocinador(idPatrocinador);
    }

    @GetMapping("/festival/{idFestival}/total")
    public BigDecimal totalPorFestival(@PathVariable long idFestival) {
        return patrocinioService.totalPorFestival(idFestival);
    }

    @PreAuthorize("hasAnyRole('ADMINISTRADOR', 'ORGANIZADOR')")
    @PostMapping
    public ResponseEntity<?> crear(@RequestBody PatrocinioDTO dto, @AuthenticationPrincipal Usuario actual) {
        if (!(puedeGestionar(dto.getIdFestival(), actual))) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("No puedes gestionar recursos de este festival");
        }
        return ResponseEntity.status(HttpStatus.CREATED).body(patrocinioService.crear(dto));
    }

    @PreAuthorize("hasAnyRole('ADMINISTRADOR', 'ORGANIZADOR')")
    @PutMapping("/{id}")
    public ResponseEntity<?> actualizar(@PathVariable long id, @RequestBody PatrocinioDTO dto, @AuthenticationPrincipal Usuario actual) {
        if (!(puedeGestionar(patrocinioService.buscarPorId(id).getIdFestival(), actual) && puedeGestionar(dto.getIdFestival(), actual))) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("No puedes gestionar recursos de este festival");
        }
        return ResponseEntity.ok(patrocinioService.actualizar(id, dto));
    }

    @PreAuthorize("hasAnyRole('ADMINISTRADOR', 'ORGANIZADOR')")
    @DeleteMapping("/{id}")
    public ResponseEntity<?> eliminar(@PathVariable long id, @AuthenticationPrincipal Usuario actual) {
        if (!(puedeGestionar(patrocinioService.buscarPorId(id).getIdFestival(), actual))) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("No puedes gestionar recursos de este festival");
        }
        patrocinioService.eliminar(id);
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
