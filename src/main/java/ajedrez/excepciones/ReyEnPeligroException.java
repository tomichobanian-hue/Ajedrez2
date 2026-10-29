package ajedrez.excepciones;

/** Caso particular de jugada invalida: deja al propio rey en jaque o no resuelve el jaque. */
public class ReyEnPeligroException extends MovimientoInvalidoException {
    public ReyEnPeligroException(String motivo) {
        super(motivo);
    }
}
