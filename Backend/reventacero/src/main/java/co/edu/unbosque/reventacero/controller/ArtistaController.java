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

import co.edu.unbosque.reventacero.dto.ArtistaDTO;
import co.edu.unbosque.reventacero.service.ArtistaService;

/** Controlador de Artista. RF-04; Fase 2, semana 12. */
@RestController
@RequestMapping("/artistas")
public class ArtistaController {

    private final ArtistaService artistaService;

    public ArtistaController(ArtistaService artistaService) {
        this.artistaService = artistaService;
    }

    @GetMapping
    public List<ArtistaDTO> listar() {
        return artistaService.listar();
    }

    @GetMapping("/{id}")
    public ArtistaDTO buscarPorId(@PathVariable long id) {
        return artistaService.buscarPorId(id);
    }

    @GetMapping("/genero/{idGenero}")
    public List<ArtistaDTO> listarPorGenero(@PathVariable long idGenero) {
        return artistaService.listarPorGenero(idGenero);
    }

    @GetMapping("/nombre/{nombreArtistico}")
    public List<ArtistaDTO> buscarPorNombreArtistico(@PathVariable String nombreArtistico) {
        return artistaService.buscarPorNombreArtistico(nombreArtistico);
    }

    @PreAuthorize("hasAnyRole('ADMINISTRADOR', 'ORGANIZADOR')")
    @PostMapping
    public ResponseEntity<ArtistaDTO> crear(@RequestBody ArtistaDTO dto) {
        return ResponseEntity.status(HttpStatus.CREATED).body(artistaService.crear(dto));
    }

    @PreAuthorize("hasAnyRole('ADMINISTRADOR', 'ORGANIZADOR')")
    @PutMapping("/{id}")
    public ResponseEntity<?> actualizar(@PathVariable long id, @RequestBody ArtistaDTO dto) {
        return ResponseEntity.ok(artistaService.actualizar(id, dto));
    }

    @PreAuthorize("hasAnyRole('ADMINISTRADOR', 'ORGANIZADOR')")
    @DeleteMapping("/{id}")
    public ResponseEntity<?> eliminar(@PathVariable long id) {
        artistaService.eliminar(id);
        return ResponseEntity.noContent().build();
    }
}
