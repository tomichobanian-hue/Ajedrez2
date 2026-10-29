package ajedrez.modelo;

import java.util.Map;

/** Como se guardan las piezas. Es un componente interno de Tablero. */
public interface AlmacenPiezas {
    Pieza obtener(Posicion posicion);

    void colocar(Posicion posicion, Pieza pieza);

    Pieza quitar(Posicion posicion);

    Map<Posicion, Pieza> piezasDe(Color color);

    /** Copia independiente, incluidas las piezas. */
    AlmacenPiezas copiar();
}
