package ajedrez.modelo;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotSame;
import static org.junit.Assert.assertTrue;

import ajedrez.soporte.Fixtures;
import java.util.List;
import org.junit.Test;

public class PiezaTest {

    @Test
    public void delegaMovimientosALaStrategy() {
        Tablero t = Fixtures.tableroDesdeFen("4k3/8/8/8/8/8/4P3/4K3 w - - 0 1");
        Pieza peon = t.obtenerPieza(Fixtures.p("e2"));
        List<Posicion> viaPieza = peon.movimientosPosibles(Fixtures.p("e2"), t);
        List<Posicion> viaStrategy = new MovimientoPeon().movimientos(peon, Fixtures.p("e2"), t);
        assertEquals(viaStrategy, viaPieza);
    }

    @Test
    public void marcarMovidaCambiaHaMovido() {
        Pieza p = new Pieza(Color.BLANCO, TipoPieza.REY, new MovimientoRey());
        assertFalse(p.haMovido());
        p.marcarMovida();
        assertTrue(p.haMovido());
        p.restaurarMovida(false);
        assertFalse(p.haMovido());
    }

    @Test
    public void copiarEsIndependienteEnHaMovido() {
        Pieza original = new Pieza(Color.NEGRO, TipoPieza.TORRE, new MovimientoHorizontalVertical());
        Pieza copia = original.copiar();
        assertNotSame(original, copia);
        assertEquals(original.getColor(), copia.getColor());
        assertEquals(original.getTipo(), copia.getTipo());
        original.marcarMovida();
        assertTrue(original.haMovido());
        assertFalse(copia.haMovido());
    }
}
