package co.edu.unbosque.reventacero.controller;

import org.springframework.http.HttpStatus;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import co.edu.unbosque.reventacero.exception.RecursoNoEncontradoException;
import co.edu.unbosque.reventacero.exception.ReglaNegocioException;

/** Traduce las excepciones de los servicios a respuestas HTTP de los controladores. */
@RestControllerAdvice
public class ManejadorExcepcionesController {

    @ExceptionHandler(RecursoNoEncontradoException.class)
    public ResponseEntity<String> recursoNoEncontrado(RecursoNoEncontradoException ex) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(ex.getMessage());
    }

    @ExceptionHandler(ReglaNegocioException.class)
    public ResponseEntity<String> reglaNegocio(ReglaNegocioException ex) {
        return ResponseEntity.status(HttpStatus.CONFLICT).body(ex.getMessage());
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<String> solicitudInvalida(IllegalArgumentException ex) {
        return ResponseEntity.badRequest().body(ex.getMessage());
    }

    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<String> conflictoDeIntegridad(DataIntegrityViolationException ex) {
        return ResponseEntity.status(HttpStatus.CONFLICT)
                .body("La operacion entra en conflicto con los datos existentes");
    }
}
