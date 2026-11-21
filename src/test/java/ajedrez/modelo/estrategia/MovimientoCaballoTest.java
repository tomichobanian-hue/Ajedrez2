package ajedrez.modelo.estrategia;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import ajedrez.modelo.MovimientoCaballo;
import ajedrez.modelo.Pieza;
import ajedrez.modelo.Posicion;
import ajedrez.modelo.Tablero;
import ajedrez.soporte.Fixtures;
import java.util.List;
import org.junit.Test;

public class MovimientoCaballoTest {
    private final MovimientoCaballo estrategia = new MovimientoCaballo();

    @Test
    public void ochoMovimientosDesdeElCentro() {
        Tablero t = Fixtures.tableroDesdeFen("4k3/8/8/8/4N3/8/8/4K3 w - - 0 1");
        Pieza n = t.obtenerPieza(Fixtures.p("e4"));
        assertEquals(8, estrategia.movimientos(n, Fixtures.p("e4"), t).size());
    }

    @Test
    public void desdeLaEsquinaSoloDos() {
        Tablero t = Fixtures.tableroDesdeFen("4k3/8/8/8/8/8/8/N3K3 w - - 0 1");
        Pieza n = t.obtenerPieza(Fixtures.p("a1"));
        List<Posicion> destinos = estrategia.movimientos(n, Fixtures.p("a1"), t);
        assertEquals(2, destinos.size());
        assertTrue(destinos.contains(Fixtures.p("b3")));
        assertTrue(destinos.contains(Fixtures.p("c2")));
    }

    @Test
    public void saltaPiezas() {
        Tablero t = Fixtures.tableroDesdeFen("4k3/8/8/8/8/8/PPPPPPPP/RNBQKBNR w KQ - 0 1");
        Pieza n = t.obtenerPieza(Fixtures.p("b1"));
        assertTrue(estrategia.movimientos(n, Fixtures.p("b1"), t).contains(Fixtures.p("a3")));
        assertTrue(estrategia.movimientos(n, Fixtures.p("b1"), t).contains(Fixtures.p("c3")));
    }

    @Test
    public void noTerminaSobrePiezaPropia() {
        Tablero t = Fixtures.tableroDesdeFen("4k3/8/8/8/8/2P5/8/1N2K3 w - - 0 1");
        Pieza n = t.obtenerPieza(Fixtures.p("b1"));
        assertFalse(estrategia.movimientos(n, Fixtures.p("b1"), t).contains(Fixtures.p("c3")));
    }

    @Test
    public void puedeCapturarRival() {
        Tablero t = Fixtures.tableroDesdeFen("4k3/8/8/8/8/2p5/8/1N2K3 w - - 0 1");
        Pieza n = t.obtenerPieza(Fixtures.p("b1"));
        assertTrue(estrategia.movimientos(n, Fixtures.p("b1"), t).contains(Fixtures.p("c3")));
    }
}
