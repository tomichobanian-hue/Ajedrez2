package ajedrez.modelo;

import java.util.ArrayList;
import java.util.List;

public class MovimientoCaballo implements EstrategiaMovimiento {
    private static final int[][] SALTOS = {
        {2, 1}, {2, -1}, {-2, 1}, {-2, -1}, {1, 2}, {1, -2}, {-1, 2}, {-1, -2}};

    @Override
    public List<Posicion> movimientos(Pieza pieza, Posicion origen, TableroConsulta tablero) {
        List<Posicion> destinos = new ArrayList<>();
        for (int[] s : SALTOS) {
            Posicion p = origen.sumar(s[0], s[1]);
            if (!tablero.estaDentro(p)) continue;
            Pieza otra = tablero.obtenerPieza(p);
            if (otra == null || otra.getColor() != pieza.getColor()) destinos.add(p);
        }
        return destinos;
    }
}
