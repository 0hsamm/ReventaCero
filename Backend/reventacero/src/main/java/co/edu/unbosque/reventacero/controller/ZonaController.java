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

import co.edu.unbosque.reventacero.dto.ZonaDTO;
import co.edu.unbosque.reventacero.entity.Usuario;
import co.edu.unbosque.reventacero.enums.TipoUsuario;
import co.edu.unbosque.reventacero.service.FestivalService;
import co.edu.unbosque.reventacero.service.ZonaService;

/** Controlador de Zona. RF-02, RF-19; Fase 2, semana 12. */
@RestController
@RequestMapping("/zonas")
public class ZonaController {

    private final ZonaService zonaService;

    private final FestivalService festivalService;

    public ZonaController(ZonaService zonaService, FestivalService festivalService) {
        this.zonaService = zonaService;
        this.festivalService = festivalService;
    }

    @GetMapping
    public List<ZonaDTO> listar() {
        return zonaService.listar();
    }

    @GetMapping("/{id}")
    public ZonaDTO buscarPorId(@PathVariable long id) {
        return zonaService.buscarPorId(id);
    }

    @GetMapping("/festival/{idFestival}")
    public List<ZonaDTO> listarPorFestival(@PathVariable long idFestival) {
        return zonaService.listarPorFestival(idFestival);
    }

    @GetMapping("/{idZona}/aforo-disponible")
    public int aforoDisponible(@PathVariable long idZona) {
        return zonaService.aforoDisponible(idZona);
    }

    @PreAuthorize("hasAnyRole('ADMINISTRADOR', 'ORGANIZADOR')")
    @PostMapping
    public ResponseEntity<?> crear(@RequestBody ZonaDTO dto, @AuthenticationPrincipal Usuario actual) {
        if (!(puedeGestionar(dto.getIdFestival(), actual))) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("No puedes gestionar recursos de este festival");
        }
        return ResponseEntity.status(HttpStatus.CREATED).body(zonaService.crear(dto));
    }

    @PreAuthorize("hasAnyRole('ADMINISTRADOR', 'ORGANIZADOR')")
    @PutMapping("/{id}")
    public ResponseEntity<?> actualizar(@PathVariable long id, @RequestBody ZonaDTO dto, @AuthenticationPrincipal Usuario actual) {
        if (!(puedeGestionar(zonaService.buscarPorId(id).getIdFestival(), actual) && puedeGestionar(dto.getIdFestival(), actual))) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("No puedes gestionar recursos de este festival");
        }
        return ResponseEntity.ok(zonaService.actualizar(id, dto));
    }

    @PreAuthorize("hasAnyRole('ADMINISTRADOR', 'ORGANIZADOR')")
    @DeleteMapping("/{id}")
    public ResponseEntity<?> eliminar(@PathVariable long id, @AuthenticationPrincipal Usuario actual) {
        if (!(puedeGestionar(zonaService.buscarPorId(id).getIdFestival(), actual))) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("No puedes gestionar recursos de este festival");
        }
        zonaService.eliminar(id);
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
