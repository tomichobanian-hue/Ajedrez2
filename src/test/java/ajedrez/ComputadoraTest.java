package ajedrez;

import static ajedrez.Utiles.p;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import ajedrez.ia.AdaptadorMinimax;
import ajedrez.ia.JugadorComputadora;
import ajedrez.ia.MotorMinimax;
import ajedrez.ia.NivelIA;
import ajedrez.modelo.AlmacenPorMapa;
import ajedrez.modelo.Color;
import ajedrez.modelo.Jugada;
import ajedrez.modelo.Tablero;
import ajedrez.modelo.TipoPieza;
import ajedrez.partida.Juego;
import java.util.List;
import java.util.Optional;
import java.util.Random;
import org.junit.Test;

/** MotorMinimax (Adaptee), AdaptadorMinimax (Adapter) y Juego usando solo JugadorComputadora (Target). */
public class ComputadoraTest {
    static final String DAMA_SUELTA = "4k3/8/8/3q4/8/8/3R4/4K3 w - - 0 1";
    static final String MATE_EN_UNA = "6k1/5ppp/8/8/8/8/8/R5K1 w - - 0 1";
    static final String NEGRAS_MATEADAS = "R5k1/5ppp/8/8/8/8/8/6K1 b - - 0 1";

    private static Tablero tablero(String fen) {
        return Utiles.tableroDesdeFen(new AlmacenPorMapa(), fen);
    }

    private static JugadorComputadora computadora(NivelIA nivel) {
        return new AdaptadorMinimax(new MotorMinimax(new Random(1)), nivel, new Random(1));
    }

    // ---- MotorMinimax ----

    @Test
    public void elMotorVeElMateEnUna() {
        Optional<Jugada> jugada = new MotorMinimax(new Random(1)).mejorJugada(tablero(MATE_EN_UNA), Color.BLANCO, 2);
        assertEquals(new Jugada(p("a1"), p("a8")), jugada.get());
    }

    @Test
    public void elMotorPuntuaTodasDeMejorAPeor() {
        List<MotorMinimax.JugadaEvaluada> evaluadas =
                new MotorMinimax(new Random(1)).evaluarTodas(tablero(DAMA_SUELTA), Color.BLANCO, 2);
        assertEquals(new Jugada(p("d2"), p("d5")), evaluadas.get(0).jugada());
        for (int i = 1; i < evaluadas.size(); i++) {
            assertTrue(evaluadas.get(i - 1).puntaje() >= evaluadas.get(i).puntaje());
        }
    }

    // ---- AdaptadorMinimax ----

    @Test
    public void todosLosNivelesCapturanLaDamaSuelta() {
        for (NivelIA nivel : NivelIA.values()) {
            Optional<Jugada> jugada = computadora(nivel).elegirJugada(tablero(DAMA_SUELTA), Color.BLANCO);
            assertEquals(nivel.toString(), new Jugada(p("d2"), p("d5")), jugada.get());
        }
    }

    @Test
    public void sinJugadasNoEligeNada() {
        for (NivelIA nivel : NivelIA.values()) {
            assertTrue(nivel.toString(), computadora(nivel).elegirJugada(tablero(NEGRAS_MATEADAS), Color.NEGRO).isEmpty());
        }
    }

    // ---- Juego ----

    @Test
    public void elJuegoAceptaCualquierJugadorComputadora() {
        Juego juego = Utiles.juegoInicial(new AlmacenPorMapa());
        JugadorComputadora siempreE4 = (copia, turno) -> Optional.of(new Jugada(p("e2"), p("e4")));
        assertTrue(juego.jugarComputadora(siempreE4));
        assertEquals(TipoPieza.PEON, juego.getTablero().obtenerPieza(p("e4")).getTipo());
        assertEquals(Color.NEGRO, juego.getTurnoActual().get());
        juego.detener();
    }

    @Test
    public void laComputadoraMueveEnLaPartida() {
        Juego juego = Utiles.juegoInicial(new AlmacenPorMapa());
        assertTrue(juego.jugarComputadora(computadora(NivelIA.DIFICIL)));
        assertEquals(Color.NEGRO, juego.getTurnoActual().get());
        juego.detener();
    }
}
