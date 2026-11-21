package ajedrez.ia;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import ajedrez.modelo.Color;
import ajedrez.modelo.Jugada;
import ajedrez.soporte.Fixtures;
import java.util.Optional;
import java.util.Random;
import org.junit.Test;

/** Complementa ComputadoraTest: profundidad/margen y determinismo de FACIL. */
public class AdaptadorMinimaxTest {
    private static final String DAMA = "4k3/8/8/3q4/8/8/3R4/4K3 w - - 0 1";

    @Test
    public void nivelesExponenProfundidadYMargen() {
        assertEquals(1, NivelIA.FACIL.getProfundidad());
        assertEquals(150, NivelIA.FACIL.getMargen());
        assertEquals(2, NivelIA.MEDIO.getProfundidad());
        assertEquals(0, NivelIA.MEDIO.getMargen());
        assertEquals(3, NivelIA.DIFICIL.getProfundidad());
        assertEquals(0, NivelIA.DIFICIL.getMargen());
    }

    @Test
    public void facilConMismaSemillaEsDeterminista() {
        JugadorComputadora a = new AdaptadorMinimax(new MotorMinimax(new Random(1)), NivelIA.FACIL, new Random(1));
        JugadorComputadora b = new AdaptadorMinimax(new MotorMinimax(new Random(1)), NivelIA.FACIL, new Random(1));
        Optional<Jugada> ja = a.elegirJugada(Fixtures.tableroDesdeFen(DAMA), Color.BLANCO);
        Optional<Jugada> jb = b.elegirJugada(Fixtures.tableroDesdeFen(DAMA), Color.BLANCO);
        assertEquals(ja, jb);
        assertTrue(ja.isPresent());
    }
}
