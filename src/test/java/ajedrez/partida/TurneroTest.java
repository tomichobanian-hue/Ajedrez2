package ajedrez.partida;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import ajedrez.modelo.Color;
import org.junit.Test;

public class TurneroTest {

    @Test
    public void comienzaEnBlanco() {
        Turnero t = new Turnero();
        assertEquals(Color.BLANCO, t.getTurnoActual());
        assertTrue(t.esTurnoDe(Color.BLANCO));
        assertFalse(t.esTurnoDe(Color.NEGRO));
    }

    @Test
    public void pasarTurnoAlterna() {
        Turnero t = new Turnero();
        t.pasarTurno();
        assertEquals(Color.NEGRO, t.getTurnoActual());
        t.pasarTurno();
        assertEquals(Color.BLANCO, t.getTurnoActual());
    }
}
