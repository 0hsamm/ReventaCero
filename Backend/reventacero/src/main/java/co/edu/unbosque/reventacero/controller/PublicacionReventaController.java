package co.edu.unbosque.reventacero.controller;

import java.math.BigDecimal;
import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import co.edu.unbosque.reventacero.dto.PublicacionReventaDTO;
import co.edu.unbosque.reventacero.entity.Usuario;
import co.edu.unbosque.reventacero.service.BoletaService;
import co.edu.unbosque.reventacero.service.PublicacionReventaService;

@RestController
@RequestMapping("/publicaciones-reventa")
public class PublicacionReventaController {

    private static final String NO_ES_TUYA = "La boleta no pertenece a tu cuenta";

    private final PublicacionReventaService publicacionService;

    private final BoletaService boletaService;

    public PublicacionReventaController(PublicacionReventaService publicacionService, BoletaService boletaService) {
        this.publicacionService = publicacionService;
        this.boletaService = boletaService;
    }

    /** Público: la página del festival muestra las reventas también a quien no ha iniciado sesión. */
    @GetMapping
    public List<PublicacionReventaDTO> disponibles(@AuthenticationPrincipal Usuario actual) {
        return publicacionService.disponibles(actual != null ? actual.getDocumento() : 0);
    }

    @GetMapping("/mias")
    public List<PublicacionReventaDTO> mias(@AuthenticationPrincipal Usuario actual) {
        return publicacionService.delVendedor(actual.getDocumento());
    }

    @PostMapping
    public ResponseEntity<?> publicar(@RequestBody Publicacion publicacion, @AuthenticationPrincipal Usuario actual) {
        if (!esDueno(publicacion.codigoBoleta(), actual)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(NO_ES_TUYA);
        }
        publicacionService.publicar(publicacion.codigoBoleta(), publicacion.precio());
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

    @PreAuthorize("hasRole('USUARIO')")
    @PutMapping("/{referencia}/comprar")
    public ResponseEntity<?> comprar(@PathVariable long referencia, @RequestBody Compra compra, @AuthenticationPrincipal Usuario actual) {
        publicacionService.comprar(referencia, actual.getDocumento(), compra.idDispositivo(), compra.metodoPago());
        return ResponseEntity.noContent().build();
    }

    @PutMapping("/boleta/{codigoBoleta}/retirar")
    public ResponseEntity<?> retirar(@PathVariable long codigoBoleta, @AuthenticationPrincipal Usuario actual) {
        if (!esDueno(codigoBoleta, actual)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(NO_ES_TUYA);
        }
        publicacionService.retirar(codigoBoleta);
        return ResponseEntity.noContent().build();
    }

    public record Publicacion(long codigoBoleta, BigDecimal precio) {
    }

    public record Compra(long idDispositivo, String metodoPago) {
    }

    private boolean esDueno(long codigoBoleta, Usuario actual) {
        return boletaService.buscarPorCodigo(codigoBoleta).getDocumentoComprador() == actual.getDocumento();
    }
}
