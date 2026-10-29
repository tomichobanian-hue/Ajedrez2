package ajedrez.modelo;

import java.util.ArrayList;
import java.util.List;

/** Movimiento de la reina: compone el de la torre y el del alfil. */
public class MovimientoReina implements EstrategiaMovimiento {
    private final MovimientoHorizontalVertical horizontalVertical = new MovimientoHorizontalVertical();
    private final MovimientoDiagonal diagonal = new MovimientoDiagonal();

    @Override
    public List<Posicion> movimientos(Pieza pieza, Posicion origen, TableroConsulta tablero) {
        List<Posicion> destinos = new ArrayList<>(horizontalVertical.movimientos(pieza, origen, tablero));
        destinos.addAll(diagonal.movimientos(pieza, origen, tablero));
        return destinos;
    }
}
