package ajedrez.modelo;

import java.util.ArrayList;
import java.util.List;

/** Movimiento del alfil: desliza en diagonal. */
public class MovimientoDiagonal implements EstrategiaMovimiento {
    private static final int[][] DIRECCIONES = {{1, 1}, {1, -1}, {-1, 1}, {-1, -1}};

    @Override
    public List<Posicion> movimientos(Pieza pieza, Posicion origen, TableroConsulta tablero) {
        List<Posicion> destinos = new ArrayList<>();
        for (int[] d : DIRECCIONES) {
            Posicion p = origen.sumar(d[0], d[1]);
            while (tablero.estaDentro(p)) {
                Pieza otra = tablero.obtenerPieza(p);
                if (otra == null) {
                    destinos.add(p);
                } else {
                    if (otra.getColor() != pieza.getColor()) destinos.add(p);
                    break;
                }
                p = p.sumar(d[0], d[1]);
            }
        }
        return destinos;
    }
}
