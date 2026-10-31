package ajedrez.ia;

import ajedrez.modelo.Color;
import ajedrez.modelo.Jugada;
import ajedrez.modelo.Tablero;
import java.util.Optional;

/** Lo que el juego necesita de la computadora (Target del Adapter): dada la posicion, que jugada hace. */
public interface JugadorComputadora {
    /** Jugada para el color que mueve. Recibe una copia del tablero; vacio si no tiene ninguna. */
    Optional<Jugada> elegirJugada(Tablero copia, Color turno);
}
