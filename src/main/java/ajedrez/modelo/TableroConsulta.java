package ajedrez.modelo;

import java.util.Map;
import java.util.Optional;

/** Vista de solo lectura del tablero. */
public interface TableroConsulta {
    Pieza obtenerPieza(Posicion posicion);

    boolean estaOcupada(Posicion posicion);

    boolean estaDentro(Posicion posicion);

    Map<Posicion, Pieza> piezasDe(Color color);

    Posicion buscarRey(Color color);

    Optional<Movimiento> getUltimoMovimiento();

    TipoMovimiento clasificar(Jugada jugada);

    /** Devuelve una copia del tablero con la jugada ya aplicada. El tablero real no se toca. */
    TableroConsulta simular(Jugada jugada);
}
