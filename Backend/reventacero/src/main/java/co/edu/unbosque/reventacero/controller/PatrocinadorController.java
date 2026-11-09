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

import co.edu.unbosque.reventacero.dto.PatrocinadorDTO;
import co.edu.unbosque.reventacero.service.PatrocinadorService;

/** Controlador de Patrocinador. RF-08; Fase 2, semana 12. */
@RestController
@RequestMapping("/patrocinadores")
public class PatrocinadorController {

    private final PatrocinadorService patrocinadorService;

    public PatrocinadorController(PatrocinadorService patrocinadorService) {
        this.patrocinadorService = patrocinadorService;
    }

    @GetMapping
    public List<PatrocinadorDTO> listar() {
        return patrocinadorService.listar();
    }

    @GetMapping("/{id}")
    public PatrocinadorDTO buscarPorId(@PathVariable long id) {
        return patrocinadorService.buscarPorId(id);
    }

    @PreAuthorize("hasAnyRole('ADMINISTRADOR', 'ORGANIZADOR')")
    @PostMapping
    public ResponseEntity<PatrocinadorDTO> crear(@RequestBody PatrocinadorDTO dto) {
        return ResponseEntity.status(HttpStatus.CREATED).body(patrocinadorService.crear(dto));
    }

    @PreAuthorize("hasAnyRole('ADMINISTRADOR', 'ORGANIZADOR')")
    @PutMapping("/{id}")
    public ResponseEntity<?> actualizar(@PathVariable long id, @RequestBody PatrocinadorDTO dto) {
        return ResponseEntity.ok(patrocinadorService.actualizar(id, dto));
    }

    @PreAuthorize("hasAnyRole('ADMINISTRADOR', 'ORGANIZADOR')")
    @DeleteMapping("/{id}")
    public ResponseEntity<?> eliminar(@PathVariable long id) {
        patrocinadorService.eliminar(id);
        return ResponseEntity.noContent().build();
    }
}
