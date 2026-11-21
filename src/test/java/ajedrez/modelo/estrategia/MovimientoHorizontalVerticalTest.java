package ajedrez.modelo.estrategia;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import ajedrez.modelo.MovimientoHorizontalVertical;
import ajedrez.modelo.Pieza;
import ajedrez.modelo.Posicion;
import ajedrez.modelo.Tablero;
import ajedrez.soporte.Fixtures;
import java.util.List;
import org.junit.Test;

public class MovimientoHorizontalVerticalTest {
    private final MovimientoHorizontalVertical estrategia = new MovimientoHorizontalVertical();

    @Test
    public void tableroVacioRecorreLineas() {
        Tablero t = Fixtures.tableroDesdeFen("4k3/8/8/3R4/8/8/8/4K3 w - - 0 1");
        Pieza torre = t.obtenerPieza(Fixtures.p("d5"));
        List<Posicion> destinos = estrategia.movimientos(torre, Fixtures.p("d5"), t);
        assertTrue(destinos.contains(Fixtures.p("d8")));
        assertTrue(destinos.contains(Fixtures.p("d1")));
        assertTrue(destinos.contains(Fixtures.p("a5")));
        assertTrue(destinos.contains(Fixtures.p("h5")));
        assertEquals(14, destinos.size());
    }

    @Test
    public void bordeNoSaleDelTablero() {
        Tablero t = Fixtures.tableroDesdeFen("4k3/8/8/8/8/8/8/R3K3 w - - 0 1");
        Pieza torre = t.obtenerPieza(Fixtures.p("a1"));
        for (Posicion p : estrategia.movimientos(torre, Fixtures.p("a1"), t)) {
            assertTrue(t.estaDentro(p));
        }
    }

    @Test
    public void bloqueoConPiezaPropia() {
        Tablero t = Fixtures.tableroDesdeFen("4k3/8/8/8/8/8/P7/R3K3 w - - 0 1");
        Pieza torre = t.obtenerPieza(Fixtures.p("a1"));
        List<Posicion> destinos = estrategia.movimientos(torre, Fixtures.p("a1"), t);
        assertFalse(destinos.contains(Fixtures.p("a2")));
        assertFalse(destinos.contains(Fixtures.p("a3")));
    }

    @Test
    public void capturaRivalYNoContinuaDetras() {
        Tablero t = Fixtures.tableroDesdeFen("4k3/8/8/8/8/8/p7/R3K3 w - - 0 1");
        Pieza torre = t.obtenerPieza(Fixtures.p("a1"));
        List<Posicion> destinos = estrategia.movimientos(torre, Fixtures.p("a1"), t);
        assertTrue(destinos.contains(Fixtures.p("a2")));
        assertFalse(destinos.contains(Fixtures.p("a3")));
    }
}
