package co.edu.unbosque.reventacero.controller;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import co.edu.unbosque.reventacero.dto.ControlAccesoDTO;
import co.edu.unbosque.reventacero.entity.Usuario;
import co.edu.unbosque.reventacero.enums.TipoUsuario;
import co.edu.unbosque.reventacero.service.BoletaService;
import co.edu.unbosque.reventacero.service.ControlAccesoService;
import co.edu.unbosque.reventacero.service.FestivalService;
import co.edu.unbosque.reventacero.service.TipoEntradaService;
import co.edu.unbosque.reventacero.service.ZonaService;

/** RF-17; controlador de ControlAcceso de Fase 2, semana 12. */
@RestController
@RequestMapping("/controles-acceso")
public class ControlAccesoController {

    private final ControlAccesoService controlAccesoService;

    private final BoletaService boletaService;

    private final TipoEntradaService tipoEntradaService;

    private final ZonaService zonaService;

    private final FestivalService festivalService;

    public ControlAccesoController(ControlAccesoService controlAccesoService, BoletaService boletaService, TipoEntradaService tipoEntradaService, ZonaService zonaService, FestivalService festivalService) {
        this.controlAccesoService = controlAccesoService;
        this.boletaService = boletaService;
        this.tipoEntradaService = tipoEntradaService;
        this.zonaService = zonaService;
        this.festivalService = festivalService;
    }

    @PreAuthorize("hasRole('ADMINISTRADOR')")
    @GetMapping
    public List<ControlAccesoDTO> listar() {
        return controlAccesoService.listar();
    }

    @PreAuthorize("hasAnyRole('ADMINISTRADOR', 'ORGANIZADOR')")
    @GetMapping("/{id}")
    public ResponseEntity<?> buscarPorId(@PathVariable long id, @AuthenticationPrincipal Usuario actual) {
        ControlAccesoDTO dto = controlAccesoService.buscarPorId(id);
        if (!(puedeGestionarBoleta(dto.getCodigoBoleta(), actual))) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("No eres el organizador de este festival");
        }
        return ResponseEntity.ok(dto);
    }

    @PreAuthorize("hasAnyRole('ADMINISTRADOR', 'ORGANIZADOR')")
    @GetMapping("/boleta/{codigoBoleta}")
    public ResponseEntity<?> buscarPorBoleta(@PathVariable long codigoBoleta, @AuthenticationPrincipal Usuario actual) {
        if (!(puedeGestionarBoleta(codigoBoleta, actual))) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("No eres el organizador de este festival");
        }
        return ResponseEntity.ok(controlAccesoService.buscarPorBoleta(codigoBoleta));
    }

    @PreAuthorize("hasAnyRole('ADMINISTRADOR', 'ORGANIZADOR')")
    @PostMapping("/boleta/{codigoBoleta}")
    public ResponseEntity<?> registrarIngreso(@PathVariable long codigoBoleta, @AuthenticationPrincipal Usuario actual) {
        if (!(puedeGestionarBoleta(codigoBoleta, actual))) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("No eres el organizador de este festival");
        }
        ControlAccesoDTO respuesta = controlAccesoService.registrarIngreso(codigoBoleta);
        if (ControlAccesoService.DENEGADO.equals(respuesta.getResultado())) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(respuesta);
        }
        return ResponseEntity.status(HttpStatus.CREATED).body(respuesta);
    }

    private boolean esAdministrador(Usuario actual) {
        return actual != null && actual.getTipoUsuario() == TipoUsuario.ADMINISTRADOR;
    }

    private boolean puedeGestionar(long idFestival, Usuario actual) {
        if (esAdministrador(actual)) {
            return true;
        }
        return actual != null && actual.getTipoUsuario() == TipoUsuario.ORGANIZADOR
            && festivalService.buscarPorId(idFestival).getDocumentoOrganizador() == actual.getDocumento();
    }

    private boolean puedeGestionarTipoEntrada(long idTipoEntrada, Usuario actual) {
        if (esAdministrador(actual)) {
            return true;
        }
        long idZona = tipoEntradaService.buscarPorId(idTipoEntrada).getIdZona();
        return puedeGestionar(zonaService.buscarPorId(idZona).getIdFestival(), actual);
    }

    private boolean puedeGestionarBoleta(long codigoBoleta, Usuario actual) {
        return esAdministrador(actual) || puedeGestionarTipoEntrada(boletaService.buscarPorCodigo(codigoBoleta).getIdTipoEntrada(), actual);
    }
}
