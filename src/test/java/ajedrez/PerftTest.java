package ajedrez;

import static org.junit.Assert.assertEquals;

import ajedrez.excepciones.MovimientoInvalidoException;
import ajedrez.modelo.AlmacenPiezas;
import ajedrez.modelo.AlmacenPorMapa;
import ajedrez.modelo.AlmacenPorMatriz;
import ajedrez.modelo.Color;
import ajedrez.modelo.Jugada;
import ajedrez.modelo.Movimiento;
import ajedrez.modelo.Pieza;
import ajedrez.modelo.Posicion;
import ajedrez.modelo.Tablero;
import ajedrez.modelo.TipoPieza;
import ajedrez.validacion.ValidadorJaque;
import ajedrez.validacion.ValidadorJugada;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import org.junit.Test;

/** Cuenta posiciones alcanzables (perft) y las compara con los valores conocidos del ajedrez. */
public class PerftTest {
    private final ValidadorJugada vj = Utiles.validadorJugada();
    private final ValidadorJaque vk = new ValidadorJaque();

    private List<Jugada> legales(Tablero t, Color turno) {
        List<Jugada> lista = new ArrayList<>();
        for (Map.Entry<Posicion, Pieza> e : t.piezasDe(turno).entrySet()) {
            Posicion o = e.getKey();
            Pieza pieza = e.getValue();
            for (Posicion d : pieza.movimientosPosibles(o, t)) {
                boolean promo = pieza.getTipo() == TipoPieza.PEON && d.fila() == (turno == Color.BLANCO ? 7 : 0);
                TipoPieza[] opciones = promo
                        ? new TipoPieza[] {TipoPieza.REINA, TipoPieza.TORRE, TipoPieza.ALFIL, TipoPieza.CABALLO}
                        : new TipoPieza[] {null};
                for (TipoPieza op : opciones) {
                    Jugada j = new Jugada(o, d, op);
                    try {
                        vj.validar(t, j);
                        vk.validar(t, j);
                        lista.add(j);
                    } catch (MovimientoInvalidoException ex) {
                        // ilegal
                    }
                }
            }
        }
        return lista;
    }

    private long perft(Tablero t, Color turno, int profundidad) {
        if (profundidad == 0) return 1;
        long total = 0;
        for (Jugada j : legales(t, turno)) {
            Movimiento m = t.mover(j);
            total += perft(t, turno.opuesto(), profundidad - 1);
            t.deshacer(m);
        }
        return total;
    }

    private void verificar(AlmacenPiezas almacen, String fen, Color turno, long... esperados) {
        Tablero t = Utiles.tableroDesdeFen(almacen, fen);
        for (int i = 0; i < esperados.length; i++) {
            assertEquals("profundidad " + (i + 1) + " de " + fen, esperados[i], perft(t, turno, i + 1));
        }
    }

    static final String INICIAL = "rnbqkbnr/pppppppp/8/8/8/8/PPPPPPPP/RNBQKBNR w KQkq - 0 1";
    static final String KIWIPETE = "r3k2r/p1ppqpb1/bn2pnp1/3PN3/1p2P3/2N2Q1p/PPPBBPPP/R3K2R w KQkq - 0 1";
    static final String POS3 = "8/2p5/3p4/KP5r/1R3p1k/8/4P1P1/8 w - - 0 1";
    static final String POS4 = "r3k2r/Pppp1ppp/1b3nbN/nP6/BBP1P3/q4N2/Pp1P2PP/R2Q1RK1 w kq - 0 1";
    static final String POS5 = "rnbq1k1r/pp1Pbppp/2p5/8/2B5/8/PPP1NnPP/RNBQK2R w KQ - 1 8";

    @Test
    public void posicionInicial() {
        verificar(new AlmacenPorMapa(), INICIAL, Color.BLANCO, 20, 400, 8902);
    }

    @Test
    public void posicionInicialConMatriz() {
        verificar(new AlmacenPorMatriz(), INICIAL, Color.BLANCO, 20, 400, 8902);
    }

    @Test
    public void kiwipete() {
        verificar(new AlmacenPorMapa(), KIWIPETE, Color.BLANCO, 48, 2039, 97862);
    }

    @Test
    public void posicion3() {
        verificar(new AlmacenPorMapa(), POS3, Color.BLANCO, 14, 191, 2812, 43238);
    }

    @Test
    public void posicion4() {
        verificar(new AlmacenPorMapa(), POS4, Color.BLANCO, 6, 264, 9467);
    }

    @Test
    public void posicion5() {
        verificar(new AlmacenPorMapa(), POS5, Color.BLANCO, 44, 1486, 62379);
    }
}
