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

import co.edu.unbosque.reventacero.dto.OrganizadorDTO;
import co.edu.unbosque.reventacero.service.OrganizadorService;

/** Controlador de Organizador. RF-10; Fase 2, semana 12. */
@RestController
@RequestMapping("/organizadores")
@PreAuthorize("hasRole('ADMINISTRADOR')")
public class OrganizadorController {

    private final OrganizadorService organizadorService;

    public OrganizadorController(OrganizadorService organizadorService) {
        this.organizadorService = organizadorService;
    }

    @GetMapping
    public List<OrganizadorDTO> listar() {
        return organizadorService.listar();
    }

    @GetMapping("/{documento}")
    public OrganizadorDTO buscarPorDocumento(@PathVariable long documento) {
        return organizadorService.buscarPorDocumento(documento);
    }

    @GetMapping("/supervisor/{documentoSupervisor}")
    public List<OrganizadorDTO> listarSupervisados(@PathVariable long documentoSupervisor) {
        return organizadorService.listarSupervisados(documentoSupervisor);
    }

    @PreAuthorize("hasRole('ADMINISTRADOR')")
    @PostMapping
    public ResponseEntity<OrganizadorDTO> crear(@RequestBody OrganizadorDTO dto) {
        return ResponseEntity.status(HttpStatus.CREATED).body(organizadorService.crear(dto));
    }

    @PreAuthorize("hasRole('ADMINISTRADOR')")
    @PutMapping("/{documento}")
    public ResponseEntity<?> actualizar(@PathVariable long documento, @RequestBody OrganizadorDTO dto) {
        return ResponseEntity.ok(organizadorService.actualizar(documento, dto));
    }

    @PreAuthorize("hasRole('ADMINISTRADOR')")
    @DeleteMapping("/{documento}")
    public ResponseEntity<?> eliminar(@PathVariable long documento) {
        organizadorService.eliminar(documento);
        return ResponseEntity.noContent().build();
    }
}
