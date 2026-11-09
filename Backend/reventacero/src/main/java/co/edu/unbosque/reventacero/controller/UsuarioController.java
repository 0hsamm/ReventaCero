package co.edu.unbosque.reventacero.controller;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import co.edu.unbosque.reventacero.dto.UsuarioDTO;
import co.edu.unbosque.reventacero.service.UsuarioService;

/** RF-10; controlador de Usuario de Fase 2, semana 12. */
@RestController
@RequestMapping("/usuarios")
@PreAuthorize("hasRole('ADMINISTRADOR')")
public class UsuarioController {

    private final UsuarioService usuarioService;

    public UsuarioController(UsuarioService usuarioService) {
        this.usuarioService = usuarioService;
    }


    @GetMapping
    public List<UsuarioDTO> listar() {
        return usuarioService.listar();
    }

    @GetMapping("/{documento}")
    public UsuarioDTO buscarPorDocumento(@PathVariable long documento) {
        return usuarioService.buscarPorDocumento(documento);
    }

    @PutMapping("/{documento}")
    public ResponseEntity<UsuarioDTO> actualizar(@PathVariable long documento, @RequestBody UsuarioDTO dto) {
        return ResponseEntity.ok(usuarioService.actualizar(documento, dto));
    }

    @PutMapping("/{documento}/estado")
    public ResponseEntity<UsuarioDTO> cambiarEstado(@PathVariable long documento, @RequestBody CambioEstado solicitud) {
        return ResponseEntity.ok(usuarioService.cambiarEstado(documento, solicitud.estado()));
    }

    @PutMapping("/{documento}/contrasenia")
    public ResponseEntity<Void> cambiarContrasenia(@PathVariable long documento, @RequestBody CambioContrasenia solicitud) {
        usuarioService.cambiarContrasenia(documento, solicitud.contraseniaActual(), solicitud.contraseniaNueva());
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/{documento}")
    public ResponseEntity<Void> eliminar(@PathVariable long documento) {
        usuarioService.eliminar(documento);
        return ResponseEntity.noContent().build();
    }

    public record CambioEstado(String estado) {
    }

    public record CambioContrasenia(String contraseniaActual, String contraseniaNueva) {
        @Override
        public String toString() {
            return "CambioContrasenia[credenciales protegidas]";
        }
    }
}
