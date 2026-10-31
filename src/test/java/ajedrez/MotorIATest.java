package ajedrez;

import static ajedrez.Utiles.p;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import ajedrez.ia.MotorIA;
import ajedrez.ia.NivelIA;
import ajedrez.modelo.AlmacenPorMapa;
import ajedrez.modelo.Color;
import ajedrez.modelo.Jugada;
import ajedrez.modelo.Tablero;
import ajedrez.partida.Juego;
import java.util.Optional;
import java.util.Random;
import org.junit.Test;

public class MotorIATest {

    private Optional<Jugada> elegir(NivelIA nivel, String fen, Color turno) {
        Tablero tablero = Utiles.tableroDesdeFen(new AlmacenPorMapa(), fen);
        return new MotorIA(nivel, new Random(1)).elegir(tablero, turno);
    }

    @Test
    public void capturaLaDamaQueQuedaSuelta() {
        String fen = "4k3/8/8/3q4/8/8/3R4/4K3 w - - 0 1";
        for (NivelIA nivel : NivelIA.values()) {
            assertEquals(nivel.toString(), new Jugada(p("d2"), p("d5")), elegir(nivel, fen, Color.BLANCO).get());
        }
    }

    @Test
    public void veElMateEnUna() {
        String fen = "6k1/5ppp/8/8/8/8/8/R5K1 w - - 0 1";
        assertEquals(new Jugada(p("a1"), p("a8")), elegir(NivelIA.MEDIO, fen, Color.BLANCO).get());
        assertEquals(new Jugada(p("a1"), p("a8")), elegir(NivelIA.DIFICIL, fen, Color.BLANCO).get());
    }

    @Test
    public void sinJugadasNoEligeNada() {
        String fen = "R5k1/5ppp/8/8/8/8/8/6K1 b - - 0 1";
        assertTrue(elegir(NivelIA.MEDIO, fen, Color.NEGRO).isEmpty());
    }

    @Test
    public void laComputadoraMueveEnLaPartida() {
        Juego juego = Utiles.juegoInicial(new AlmacenPorMapa());
        assertTrue(juego.jugarComputadora(new MotorIA(NivelIA.DIFICIL, new Random(1))));
        assertEquals(Color.NEGRO, juego.getTurnoActual().get());
        assertTrue(juego.getTablero().getUltimoMovimiento().isPresent());
        juego.detener();
    }
}
