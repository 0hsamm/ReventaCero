package co.edu.unbosque.reventacero.controller;

import java.util.List;

import org.springframework.http.CacheControl;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import co.edu.unbosque.reventacero.dto.BoletaDTO;
import co.edu.unbosque.reventacero.entity.Usuario;
import co.edu.unbosque.reventacero.enums.TipoUsuario;
import co.edu.unbosque.reventacero.service.BoletaQrService;
import co.edu.unbosque.reventacero.service.BoletaService;

/** RF-12, RF-20; controlador de Boleta de Fase 2, semana 12. */
@RestController
@RequestMapping("/boletas")
public class BoletaController {

    private final BoletaService boletaService;

    private final BoletaQrService boletaQrService;

    public BoletaController(BoletaService boletaService, BoletaQrService boletaQrService) {
        this.boletaService = boletaService;
        this.boletaQrService = boletaQrService;
    }

    @PreAuthorize("hasRole('ADMINISTRADOR')")
    @GetMapping
    public List<BoletaDTO> listar() {
        return boletaService.listar();
    }

    @GetMapping("/{codigoBoleta}")
    public ResponseEntity<?> buscarPorCodigo(@PathVariable long codigoBoleta, @AuthenticationPrincipal Usuario actual) {
        BoletaDTO dto = boletaService.buscarPorCodigo(codigoBoleta);
        if (!(puedeAcceder(dto.getDocumentoComprador(), actual))) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("No tienes permiso para consultar o modificar este recurso");
        }
        return ResponseEntity.ok(dto);
    }

    @GetMapping("/{codigoBoleta}/qr")
    public ResponseEntity<?> qr(@PathVariable long codigoBoleta, @AuthenticationPrincipal Usuario actual) {
        BoletaDTO dto = boletaService.buscarPorCodigo(codigoBoleta);
        if (!(puedeAcceder(dto.getDocumentoComprador(), actual))) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).contentType(MediaType.TEXT_PLAIN)
                    .body("No tienes permiso para consultar o modificar este recurso");
        }
        return ResponseEntity.ok()
                .contentType(MediaType.valueOf("image/svg+xml"))
                .cacheControl(CacheControl.noStore())
                .body(boletaQrService.svg(codigoBoleta));
    }

    @GetMapping("/{codigoBoleta}/qr/texto")
    public ResponseEntity<String> qrTexto(@PathVariable long codigoBoleta, @AuthenticationPrincipal Usuario actual) {
        BoletaDTO dto = boletaService.buscarPorCodigo(codigoBoleta);
        if (!(puedeAcceder(dto.getDocumentoComprador(), actual))) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("No tienes permiso para consultar o modificar este recurso");
        }
        return ResponseEntity.ok()
                .contentType(new MediaType(MediaType.TEXT_PLAIN, java.nio.charset.StandardCharsets.UTF_8))
                .cacheControl(CacheControl.noStore())
                .body(boletaQrService.contenido(codigoBoleta));
    }

    @GetMapping("/comprador/{documento}")
    public ResponseEntity<?> listarPorComprador(@PathVariable long documento, @AuthenticationPrincipal Usuario actual) {
        if (!(puedeAcceder(documento, actual))) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("No tienes permiso para consultar o modificar este recurso");
        }
        return ResponseEntity.ok(boletaService.listarPorComprador(documento));
    }

    @PreAuthorize("hasRole('ADMINISTRADOR')")
    @GetMapping("/tipo-entrada/{idTipoEntrada}")
    public List<BoletaDTO> listarPorTipoEntrada(@PathVariable long idTipoEntrada) {
        return boletaService.listarPorTipoEntrada(idTipoEntrada);
    }

    private boolean esAdministrador(Usuario actual) {
        return actual != null && actual.getTipoUsuario() == TipoUsuario.ADMINISTRADOR;
    }

    private boolean puedeAcceder(long documento, Usuario actual) {
        return esAdministrador(actual) || actual != null && actual.getDocumento() == documento;
    }
}
