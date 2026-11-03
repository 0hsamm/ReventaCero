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

import co.edu.unbosque.reventacero.dto.TipoEntradaDTO;
import co.edu.unbosque.reventacero.entity.Usuario;
import co.edu.unbosque.reventacero.enums.TipoUsuario;
import co.edu.unbosque.reventacero.service.FestivalService;
import co.edu.unbosque.reventacero.service.TipoEntradaService;
import co.edu.unbosque.reventacero.service.ZonaService;

/** Controlador de TipoEntrada. RF-09; Fase 2, semana 12. */
@RestController
@RequestMapping("/tipos-entrada")
public class TipoEntradaController {

    private final TipoEntradaService tipoEntradaService;

    private final FestivalService festivalService;

    private final ZonaService zonaService;

    public TipoEntradaController(TipoEntradaService tipoEntradaService, FestivalService festivalService, ZonaService zonaService) {
        this.tipoEntradaService = tipoEntradaService;
        this.festivalService = festivalService;
        this.zonaService = zonaService;
    }

    @GetMapping
    public List<TipoEntradaDTO> listar() {
        return tipoEntradaService.listar();
    }

    @GetMapping("/{id}")
    public TipoEntradaDTO buscarPorId(@PathVariable long id) {
        return tipoEntradaService.buscarPorId(id);
    }

    @GetMapping("/zona/{idZona}")
    public List<TipoEntradaDTO> listarPorZona(@PathVariable long idZona) {
        return tipoEntradaService.listarPorZona(idZona);
    }

    @PreAuthorize("hasAnyRole('ADMINISTRADOR', 'ORGANIZADOR')")
    @PostMapping
    public ResponseEntity<?> crear(@RequestBody TipoEntradaDTO dto, @AuthenticationPrincipal Usuario actual) {
        if (!(puedeGestionarZona(dto.getIdZona(), actual))) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("No puedes gestionar recursos de este festival");
        }
        return ResponseEntity.status(HttpStatus.CREATED).body(tipoEntradaService.crear(dto));
    }

    @PreAuthorize("hasAnyRole('ADMINISTRADOR', 'ORGANIZADOR')")
    @PutMapping("/{id}")
    public ResponseEntity<?> actualizar(@PathVariable long id, @RequestBody TipoEntradaDTO dto, @AuthenticationPrincipal Usuario actual) {
        if (!(puedeGestionarZona(tipoEntradaService.buscarPorId(id).getIdZona(), actual) && puedeGestionarZona(dto.getIdZona(), actual))) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("No puedes gestionar recursos de este festival");
        }
        return ResponseEntity.ok(tipoEntradaService.actualizar(id, dto));
    }

    @PreAuthorize("hasAnyRole('ADMINISTRADOR', 'ORGANIZADOR')")
    @DeleteMapping("/{id}")
    public ResponseEntity<?> eliminar(@PathVariable long id, @AuthenticationPrincipal Usuario actual) {
        if (!(puedeGestionarZona(tipoEntradaService.buscarPorId(id).getIdZona(), actual))) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("No puedes gestionar recursos de este festival");
        }
        tipoEntradaService.eliminar(id);
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

    private boolean puedeGestionarZona(long idZona, Usuario actual) {
        return esAdministrador(actual) || puedeGestionar(zonaService.buscarPorId(idZona).getIdFestival(), actual);
    }
}
