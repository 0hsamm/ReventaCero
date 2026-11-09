package co.edu.unbosque.reventacero.controller;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import co.edu.unbosque.reventacero.dto.NotificacionDTO;
import co.edu.unbosque.reventacero.entity.Usuario;
import co.edu.unbosque.reventacero.enums.TipoUsuario;
import co.edu.unbosque.reventacero.service.NotificacionService;

/** RF-16; controlador de Notificacion de Fase 2, semana 12. */
@RestController
@RequestMapping("/notificaciones")
public class NotificacionController {

    private final NotificacionService notificacionService;

    public NotificacionController(NotificacionService notificacionService) {
        this.notificacionService = notificacionService;
    }

    @PreAuthorize("hasRole('ADMINISTRADOR')")
    @GetMapping
    public List<NotificacionDTO> listar() {
        return notificacionService.listar();
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> buscarPorId(@PathVariable long id, @AuthenticationPrincipal Usuario actual) {
        NotificacionDTO dto = notificacionService.buscarPorId(id);
        if (!(puedeAcceder(dto.getUsuarioDocumento(), actual))) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("No tienes permiso para consultar o modificar este recurso");
        }
        return ResponseEntity.ok(dto);
    }

    @GetMapping("/usuario/{documento}")
    public ResponseEntity<?> listarPorUsuario(@PathVariable long documento, @AuthenticationPrincipal Usuario actual) {
        if (!(puedeAcceder(documento, actual))) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("No tienes permiso para consultar o modificar este recurso");
        }
        return ResponseEntity.ok(notificacionService.listarPorUsuario(documento));
    }

    @GetMapping("/inscripcion/{documento}/{idLista}")
    public ResponseEntity<?> listarPorInscripcion(@PathVariable long documento, @PathVariable long idLista, @AuthenticationPrincipal Usuario actual) {
        if (!(puedeAcceder(documento, actual))) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("No tienes permiso para consultar o modificar este recurso");
        }
        return ResponseEntity.ok(notificacionService.listarPorInscripcion(documento, idLista));
    }

    private boolean esAdministrador(Usuario actual) {
        return actual != null && actual.getTipoUsuario() == TipoUsuario.ADMINISTRADOR;
    }

    private boolean puedeAcceder(long documento, Usuario actual) {
        return esAdministrador(actual) || actual != null && actual.getDocumento() == documento;
    }
}
