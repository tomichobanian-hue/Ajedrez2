package ajedrez.excepciones;

/** Indica que una jugada o una accion no es valida. Lleva el motivo como mensaje. */
public class MovimientoInvalidoException extends RuntimeException {
    public MovimientoInvalidoException(String motivo) {
        super(motivo);
    }
}
