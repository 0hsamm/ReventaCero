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

import co.edu.unbosque.reventacero.dto.GeneroDTO;
import co.edu.unbosque.reventacero.service.GeneroService;

/** Controlador de Genero. RF-04; Fase 2, semana 12. */
@RestController
@RequestMapping("/generos")
public class GeneroController {

    private final GeneroService generoService;

    public GeneroController(GeneroService generoService) {
        this.generoService = generoService;
    }

    @GetMapping
    public List<GeneroDTO> listar() {
        return generoService.listar();
    }

    @GetMapping("/{id}")
    public GeneroDTO buscarPorId(@PathVariable long id) {
        return generoService.buscarPorId(id);
    }

    @GetMapping("/{idGeneroPrincipal}/subgeneros")
    public List<GeneroDTO> listarSubgeneros(@PathVariable long idGeneroPrincipal) {
        return generoService.listarSubgeneros(idGeneroPrincipal);
    }

    @PreAuthorize("hasAnyRole('ADMINISTRADOR', 'ORGANIZADOR')")
    @PostMapping
    public ResponseEntity<GeneroDTO> crear(@RequestBody GeneroDTO dto) {
        return ResponseEntity.status(HttpStatus.CREATED).body(generoService.crear(dto));
    }

    @PreAuthorize("hasAnyRole('ADMINISTRADOR', 'ORGANIZADOR')")
    @PutMapping("/{id}")
    public ResponseEntity<?> actualizar(@PathVariable long id, @RequestBody GeneroDTO dto) {
        return ResponseEntity.ok(generoService.actualizar(id, dto));
    }

    @PreAuthorize("hasAnyRole('ADMINISTRADOR', 'ORGANIZADOR')")
    @DeleteMapping("/{id}")
    public ResponseEntity<?> eliminar(@PathVariable long id) {
        generoService.eliminar(id);
        return ResponseEntity.noContent().build();
    }
}
