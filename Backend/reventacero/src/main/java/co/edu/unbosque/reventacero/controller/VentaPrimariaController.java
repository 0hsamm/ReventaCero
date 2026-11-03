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

import co.edu.unbosque.reventacero.dto.VentaPrimariaDTO;
import co.edu.unbosque.reventacero.entity.Usuario;
import co.edu.unbosque.reventacero.enums.TipoUsuario;
import co.edu.unbosque.reventacero.service.BoletaService;
import co.edu.unbosque.reventacero.service.DispositivoService;
import co.edu.unbosque.reventacero.service.VentaPrimariaService;

/** RF-12, RF-14; controlador de VentaPrimaria de Fase 2, semana 12. */
@RestController
@RequestMapping("/ventas-primarias")
public class VentaPrimariaController {

    private final VentaPrimariaService ventaPrimariaService;

    private final BoletaService boletaService;

    private final DispositivoService dispositivoService;

    public VentaPrimariaController(VentaPrimariaService ventaPrimariaService, BoletaService boletaService, DispositivoService dispositivoService) {
        this.ventaPrimariaService = ventaPrimariaService;
        this.boletaService = boletaService;
        this.dispositivoService = dispositivoService;
    }

    @PreAuthorize("hasRole('ADMINISTRADOR')")
    @GetMapping
    public List<VentaPrimariaDTO> listar() {
        return ventaPrimariaService.listar();
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> buscarPorId(@PathVariable long id, @AuthenticationPrincipal Usuario actual) {
        VentaPrimariaDTO dto = ventaPrimariaService.buscarPorId(id);
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
        return ResponseEntity.ok(ventaPrimariaService.listarPorBoleta(codigoBoleta));
    }

    @PostMapping
    public ResponseEntity<?> comprar(@RequestBody VentaPrimariaDTO dto, @AuthenticationPrincipal Usuario actual) {
        if (!(esAdministrador(actual) || dispositivoService.buscarPorId(dto.getIdDispositivo()).getDocumentoUsuario() == actual.getDocumento())) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("El dispositivo no pertenece a tu cuenta");
        }
        return ResponseEntity.status(HttpStatus.CREATED).body(ventaPrimariaService.comprar(dto));
    }

    private boolean esAdministrador(Usuario actual) {
        return actual != null && actual.getTipoUsuario() == TipoUsuario.ADMINISTRADOR;
    }

    private boolean puedeConsultarBoleta(long codigoBoleta, Usuario actual) {
        return esAdministrador(actual) || actual != null
            && boletaService.buscarPorCodigo(codigoBoleta).getDocumentoComprador() == actual.getDocumento();
    }
}
