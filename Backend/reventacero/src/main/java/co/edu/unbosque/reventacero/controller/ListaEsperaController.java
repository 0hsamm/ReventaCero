package co.edu.unbosque.reventacero.controller;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import co.edu.unbosque.reventacero.dto.ListaEsperaDTO;
import co.edu.unbosque.reventacero.entity.Usuario;
import co.edu.unbosque.reventacero.enums.TipoUsuario;
import co.edu.unbosque.reventacero.service.FestivalService;
import co.edu.unbosque.reventacero.service.ListaEsperaService;
import co.edu.unbosque.reventacero.service.TipoEntradaService;
import co.edu.unbosque.reventacero.service.ZonaService;

/** RF-16; controlador de ListaEspera de Fase 2, semana 12. */
@RestController
@RequestMapping("/listas-espera")
public class ListaEsperaController {

    private final ListaEsperaService listaEsperaService;

    private final TipoEntradaService tipoEntradaService;

    private final ZonaService zonaService;

    private final FestivalService festivalService;

    public ListaEsperaController(ListaEsperaService listaEsperaService, TipoEntradaService tipoEntradaService, ZonaService zonaService, FestivalService festivalService) {
        this.listaEsperaService = listaEsperaService;
        this.tipoEntradaService = tipoEntradaService;
        this.zonaService = zonaService;
        this.festivalService = festivalService;
    }


    @GetMapping
    public List<ListaEsperaDTO> listar() {
        return listaEsperaService.listar();
    }

    @GetMapping("/{idLista}")
    public ListaEsperaDTO buscarPorId(@PathVariable long idLista) {
        return listaEsperaService.buscarPorId(idLista);
    }

    @GetMapping("/tipo-entrada/{idTipoEntrada}")
    public ListaEsperaDTO buscarPorTipoEntrada(@PathVariable long idTipoEntrada) {
        return listaEsperaService.buscarPorTipoEntrada(idTipoEntrada);
    }

    @PreAuthorize("hasAnyRole('ADMINISTRADOR', 'ORGANIZADOR')")
    @PostMapping
    public ResponseEntity<?> crear(@RequestBody ListaEsperaDTO dto, @AuthenticationPrincipal Usuario actual) {
        if (!(puedeGestionarTipoEntrada(dto.getIdTipoEntrada(), actual))) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("No eres el organizador de este festival");
        }
        return ResponseEntity.status(HttpStatus.CREATED).body(listaEsperaService.crear(dto.getIdTipoEntrada()));
    }

    @PreAuthorize("hasAnyRole('ADMINISTRADOR', 'ORGANIZADOR')")
    @DeleteMapping("/{idLista}")
    public ResponseEntity<?> eliminar(@PathVariable long idLista, @AuthenticationPrincipal Usuario actual) {
        if (!(puedeGestionarTipoEntrada(listaEsperaService.buscarPorId(idLista).getIdTipoEntrada(), actual))) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("No eres el organizador de este festival");
        }
        listaEsperaService.eliminar(idLista);
        return ResponseEntity.noContent().build();
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
}
