package ajedrez.ia;

import ajedrez.excepciones.MovimientoInvalidoException;
import ajedrez.modelo.Color;
import ajedrez.modelo.Jugada;
import ajedrez.modelo.Pieza;
import ajedrez.modelo.Posicion;
import ajedrez.modelo.TableroConsulta;
import ajedrez.modelo.TipoPieza;
import ajedrez.validacion.ValidadorJaque;
import ajedrez.validacion.ValidadorMovimiento;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/** Todas las jugadas legales de un color. Si un peon promociona, una jugada por cada pieza posible. */
public class GeneradorJugadas {
    private static final TipoPieza[] PROMOCIONES = {TipoPieza.REINA, TipoPieza.TORRE, TipoPieza.ALFIL, TipoPieza.CABALLO};
    private static final TipoPieza[] SIN_PROMOCION = {null};

    private final ValidadorMovimiento validadorJugada;
    private final ValidadorJaque validadorJaque;

    public GeneradorJugadas(ValidadorMovimiento validadorJugada, ValidadorJaque validadorJaque) {
        this.validadorJugada = validadorJugada;
        this.validadorJaque = validadorJaque;
    }

    public List<Jugada> legales(TableroConsulta tablero, Color turno) {
        List<Jugada> lista = new ArrayList<>();
        for (Map.Entry<Posicion, Pieza> e : tablero.piezasDe(turno).entrySet()) {
            Posicion origen = e.getKey();
            Pieza pieza = e.getValue();
            for (Posicion destino : pieza.movimientosPosibles(origen, tablero)) {
                boolean promociona = pieza.getTipo() == TipoPieza.PEON
                        && destino.fila() == (turno == Color.BLANCO ? 7 : 0);
                for (TipoPieza opcion : promociona ? PROMOCIONES : SIN_PROMOCION) {
                    Jugada jugada = new Jugada(origen, destino, opcion);
                    try {
                        validadorJugada.validar(tablero, jugada);
                        validadorJaque.validar(tablero, jugada);
                        lista.add(jugada);
                    } catch (MovimientoInvalidoException ex) {
                        // ilegal
                    }
                }
            }
        }
        return lista;
    }
}
