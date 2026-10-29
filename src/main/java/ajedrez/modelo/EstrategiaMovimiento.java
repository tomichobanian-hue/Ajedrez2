package ajedrez.modelo;

import java.util.List;

public interface EstrategiaMovimiento {

    /** Destinos permitidos por el tipo de pieza, sin mirar si el rey queda en jaque. */
    List<Posicion> movimientos(Pieza pieza, Posicion origen, TableroConsulta tablero);

    /** Casillas que la pieza amenaza. Por defecto, las mismas que sus movimientos. */
    default List<Posicion> casillasAtacadas(Pieza pieza, Posicion origen, TableroConsulta tablero) {
        return movimientos(pieza, origen, tablero);
    }
}
