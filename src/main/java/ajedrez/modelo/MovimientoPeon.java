package ajedrez.modelo;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class MovimientoPeon implements EstrategiaMovimiento {

    private static int direccion(Pieza pieza) {
        return pieza.getColor() == Color.BLANCO ? 1 : -1;
    }

    private static int filaInicial(Pieza pieza) {
        return pieza.getColor() == Color.BLANCO ? 1 : 6;
    }

    @Override
    public List<Posicion> movimientos(Pieza pieza, Posicion origen, TableroConsulta tablero) {
        List<Posicion> destinos = new ArrayList<>();
        int dir = direccion(pieza);
        Posicion uno = origen.sumar(dir, 0);
        if (tablero.estaDentro(uno) && tablero.obtenerPieza(uno) == null) {
            destinos.add(uno);
            Posicion dos = origen.sumar(2 * dir, 0);
            if (origen.fila() == filaInicial(pieza) && tablero.estaDentro(dos)
                    && tablero.obtenerPieza(dos) == null) {
                destinos.add(dos);
            }
        }
        for (int dc : new int[] {-1, 1}) {
            Posicion diag = origen.sumar(dir, dc);
            if (!tablero.estaDentro(diag)) continue;
            Pieza otra = tablero.obtenerPieza(diag);
            if (otra != null && otra.getColor() != pieza.getColor()) destinos.add(diag);
        }
        capturaAlPaso(pieza, origen, tablero).ifPresent(destinos::add);
        return destinos;
    }

    /** El peon amenaza las dos diagonales delanteras, haya o no pieza en ellas. */
    @Override
    public List<Posicion> casillasAtacadas(Pieza pieza, Posicion origen, TableroConsulta tablero) {
        List<Posicion> casillas = new ArrayList<>();
        int dir = direccion(pieza);
        for (int dc : new int[] {-1, 1}) {
            Posicion diag = origen.sumar(dir, dc);
            if (tablero.estaDentro(diag)) casillas.add(diag);
        }
        return casillas;
    }

    /** Casilla de destino de una captura al paso, si el ultimo movimiento la permite. */
    public Optional<Posicion> capturaAlPaso(Pieza pieza, Posicion origen, TableroConsulta tablero) {
        Optional<Movimiento> ultimo = tablero.getUltimoMovimiento();
        if (ultimo.isEmpty()) return Optional.empty();
        Movimiento m = ultimo.get();
        boolean avanceDoble = m.pieza().getTipo() == TipoPieza.PEON
                && m.pieza().getColor() != pieza.getColor()
                && Math.abs(m.destino().fila() - m.origen().fila()) == 2;
        if (!avanceDoble) return Optional.empty();
        boolean alLado = m.destino().fila() == origen.fila()
                && Math.abs(m.destino().columna() - origen.columna()) == 1;
        if (!alLado) return Optional.empty();
        Posicion destino = new Posicion(origen.fila() + direccion(pieza), m.destino().columna());
        if (!tablero.estaDentro(destino) || tablero.obtenerPieza(destino) != null) return Optional.empty();
        return Optional.of(destino);
    }
}
