package co.edu.unbosque.reventacero.controller;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import co.edu.unbosque.reventacero.dto.DevolucionDTO;
import co.edu.unbosque.reventacero.entity.Usuario;
import co.edu.unbosque.reventacero.enums.TipoUsuario;
import co.edu.unbosque.reventacero.service.BoletaService;
import co.edu.unbosque.reventacero.service.DevolucionService;

/** RF-14; controlador de Devolucion de Fase 2, semana 12. */
@RestController
@RequestMapping("/devoluciones")
public class DevolucionController {

    private final DevolucionService devolucionService;

    private final BoletaService boletaService;

    public DevolucionController(DevolucionService devolucionService, BoletaService boletaService) {
        this.devolucionService = devolucionService;
        this.boletaService = boletaService;
    }

    @PreAuthorize("hasRole('ADMINISTRADOR')")
    @GetMapping
    public List<DevolucionDTO> listar() {
        return devolucionService.listar();
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> buscarPorId(@PathVariable long id, @AuthenticationPrincipal Usuario actual) {
        DevolucionDTO dto = devolucionService.buscarPorId(id);
        if (!(puedeConsultarBoleta(dto.getCodigoBoleta(), actual))) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("No tienes permiso para consultar o modificar este recurso");
        }
        return ResponseEntity.ok(dto);
    }

    @GetMapping("/boleta/{codigoBoleta}")
    public ResponseEntity<?> listarPorBoleta(@PathVariable long codigoBoleta, @AuthenticationPrincipal Usuario actual) {
        if (!(puedeConsultarBoleta(codigoBoleta, actual))) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("No tienes permiso para consultar o modificar este recurso");
        }
        return ResponseEntity.ok(devolucionService.listarPorBoleta(codigoBoleta));
    }

    @PostMapping
    public ResponseEntity<?> devolver(@RequestBody DevolucionDTO dto, @AuthenticationPrincipal Usuario actual) {
        if (!(puedeConsultarBoleta(dto.getCodigoBoleta(), actual))) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("La boleta no pertenece a tu cuenta");
        }
        return ResponseEntity.status(HttpStatus.CREATED).body(devolucionService.devolver(dto));
    }

    private boolean esAdministrador(Usuario actual) {
        return actual != null && actual.getTipoUsuario() == TipoUsuario.ADMINISTRADOR;
    }

    private boolean puedeConsultarBoleta(long codigoBoleta, Usuario actual) {
        return esAdministrador(actual) || actual != null
            && boletaService.buscarPorCodigo(codigoBoleta).getDocumentoComprador() == actual.getDocumento();
    }
}
