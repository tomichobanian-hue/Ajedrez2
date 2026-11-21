package ajedrez.ia;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import ajedrez.modelo.Color;
import ajedrez.modelo.Jugada;
import ajedrez.soporte.Fixtures;
import java.util.Optional;
import java.util.Random;
import org.junit.Test;

/** Casos unitarios no cubiertos por ComputadoraTest (mate/orden ya están allí). */
public class MotorMinimaxUnitTest {
    private final MotorMinimax motor = new MotorMinimax(new Random(1));

    @Test
    public void capturaEvidenteDeDama() {
        Optional<Jugada> jugada = motor.mejorJugada(
                Fixtures.tableroDesdeFen("4k3/8/8/3q4/8/8/3R4/4K3 w - - 0 1"), Color.BLANCO, 2);
        assertEquals(new Jugada(Fixtures.p("d2"), Fixtures.p("d5")), jugada.get());
    }

    @Test
    public void sinJugadasDevuelveVacio() {
        assertTrue(motor.mejorJugada(
                Fixtures.tableroDesdeFen("R5k1/5ppp/8/8/8/8/8/6K1 b - - 0 1"), Color.NEGRO, 1).isEmpty());
    }
}
