package ajedrez.modelo;

import java.util.ArrayList;
import java.util.List;

public class MovimientoRey implements EstrategiaMovimiento {
    private static final int[][] DESPLAZAMIENTOS = {
        {1, 0}, {-1, 0}, {0, 1}, {0, -1}, {1, 1}, {1, -1}, {-1, 1}, {-1, -1}};

    @Override
    public List<Posicion> movimientos(Pieza pieza, Posicion origen, TableroConsulta tablero) {
        List<Posicion> destinos = new ArrayList<>();
        for (int[] d : DESPLAZAMIENTOS) {
            Posicion p = origen.sumar(d[0], d[1]);
            if (!tablero.estaDentro(p)) continue;
            Pieza otra = tablero.obtenerPieza(p);
            if (otra == null || otra.getColor() != pieza.getColor()) destinos.add(p);
        }
        if (puedeEnrocar(pieza, origen, tablero, 1)) destinos.add(origen.sumar(0, 2));
        if (puedeEnrocar(pieza, origen, tablero, -1)) destinos.add(origen.sumar(0, -2));
        return destinos;
    }

    /** El rey solo amenaza las casillas contiguas; el enroque no captura. */
    @Override
    public List<Posicion> casillasAtacadas(Pieza pieza, Posicion origen, TableroConsulta tablero) {
        List<Posicion> casillas = new ArrayList<>();
        for (int[] d : DESPLAZAMIENTOS) {
            Posicion p = origen.sumar(d[0], d[1]);
            if (tablero.estaDentro(p)) casillas.add(p);
        }
        return casillas;
    }

    /**
     * Enroque posible en el lado indicado (1 = corto, -1 = largo): rey y torre sin mover,
     * camino libre. Que el rey no pase por casillas atacadas lo revisa ValidadorJaque.
     */
    public boolean puedeEnrocar(Pieza rey, Posicion origen, TableroConsulta tablero, int lado) {
        if (rey.haMovido() || origen.columna() != 4) return false;
        int filaBase = rey.getColor() == Color.BLANCO ? 0 : 7;
        if (origen.fila() != filaBase) return false;
        int columnaTorre = lado > 0 ? 7 : 0;
        Pieza torre = tablero.obtenerPieza(new Posicion(filaBase, columnaTorre));
        if (torre == null || torre.getTipo() != TipoPieza.TORRE
                || torre.getColor() != rey.getColor() || torre.haMovido()) {
            return false;
        }
        for (int c = Math.min(4, columnaTorre) + 1; c < Math.max(4, columnaTorre); c++) {
            if (tablero.obtenerPieza(new Posicion(filaBase, c)) != null) return false;
        }
        return true;
    }
}
