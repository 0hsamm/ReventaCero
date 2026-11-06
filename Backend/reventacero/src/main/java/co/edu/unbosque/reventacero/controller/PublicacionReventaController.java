package co.edu.unbosque.reventacero.controller;

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

    private final PublicacionReventaService publicacionService;

    private final BoletaService boletaService;

    public PublicacionReventaController(PublicacionReventaService publicacionService, BoletaService boletaService) {
        this.publicacionService = publicacionService;
        this.boletaService = boletaService;
    }

    @GetMapping
    public List<PublicacionReventaDTO> disponibles(@AuthenticationPrincipal Usuario actual) {
        return publicacionService.disponibles(actual != null ? actual.getDocumento() : 0);
    }

    @GetMapping("/mias")
    public List<PublicacionReventaDTO> mias(@AuthenticationPrincipal Usuario actual) {
        return publicacionService.delVendedor(actual.getDocumento());
    }

    @PostMapping
    public ResponseEntity<?> publicar(@RequestBody PublicacionReventaDTO dto, @AuthenticationPrincipal Usuario actual) {
        if (boletaService.buscarPorCodigo(dto.getCodigoBoleta()).getDocumentoComprador() != actual.getDocumento()) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("La boleta no pertenece a tu cuenta");
        }
        return ResponseEntity.status(HttpStatus.CREATED).body(publicacionService.publicar(dto, actual.getDocumento()));
    }

    @PreAuthorize("hasRole('USUARIO')")
    @PutMapping("/{id}/comprar")
    public ResponseEntity<?> comprar(@PathVariable long id, @RequestBody Compra compra, @AuthenticationPrincipal Usuario actual) {
        PublicacionReventaDTO comprada = publicacionService.comprar(id, actual.getDocumento(), compra.idDispositivo(), compra.metodoPago());
        comprada.setVendedorDocumento(0);
        return ResponseEntity.ok(comprada);
    }

    @PutMapping("/{id}/retirar")
    public ResponseEntity<?> retirar(@PathVariable long id, @AuthenticationPrincipal Usuario actual) {
        if (publicacionService.buscarPorId(id).getVendedorDocumento() != actual.getDocumento()) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Solo quien publicó la boleta puede retirarla");
        }
        return ResponseEntity.ok(publicacionService.retirar(id));
    }

    public record Compra(long idDispositivo, String metodoPago) {
    }
}
