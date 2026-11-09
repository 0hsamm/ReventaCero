package co.edu.unbosque.reventacero.controller;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import co.edu.unbosque.reventacero.dto.AdministradorDTO;
import co.edu.unbosque.reventacero.service.AdministradorService;

/** Controlador de Administrador. RF-10; Fase 2, semana 12. */
@RestController
@RequestMapping("/administradores")
@PreAuthorize("hasRole('ADMINISTRADOR')")
public class AdministradorController {

    private final AdministradorService administradorService;

    public AdministradorController(AdministradorService administradorService) {
        this.administradorService = administradorService;
    }

    @GetMapping
    public List<AdministradorDTO> listar() {
        return administradorService.listar();
    }

    @GetMapping("/{documento}")
    public AdministradorDTO buscarPorDocumento(@PathVariable long documento) {
        return administradorService.buscarPorDocumento(documento);
    }

    @PreAuthorize("hasRole('ADMINISTRADOR')")
    @PostMapping
    public ResponseEntity<AdministradorDTO> crear(@RequestBody AdministradorDTO dto) {
        return ResponseEntity.status(HttpStatus.CREATED).body(administradorService.crear(dto));
    }

    @PreAuthorize("hasRole('ADMINISTRADOR')")
    @PutMapping("/{documento}")
    public ResponseEntity<?> actualizar(@PathVariable long documento, @RequestBody AdministradorDTO dto) {
        return ResponseEntity.ok(administradorService.actualizar(documento, dto));
    }

    @PreAuthorize("hasRole('ADMINISTRADOR')")
    @DeleteMapping("/{documento}")
    public ResponseEntity<?> eliminar(@PathVariable long documento) {
        administradorService.eliminar(documento);
        return ResponseEntity.noContent().build();
    }
}
