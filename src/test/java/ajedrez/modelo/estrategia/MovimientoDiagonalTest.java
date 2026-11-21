package ajedrez.modelo.estrategia;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import ajedrez.modelo.MovimientoDiagonal;
import ajedrez.modelo.Pieza;
import ajedrez.modelo.Posicion;
import ajedrez.modelo.Tablero;
import ajedrez.soporte.Fixtures;
import java.util.List;
import org.junit.Test;

public class MovimientoDiagonalTest {
    private final MovimientoDiagonal estrategia = new MovimientoDiagonal();

    @Test
    public void tableroVacioRecorreDiagonales() {
        Tablero t = Fixtures.tableroDesdeFen("4k3/8/8/8/8/8/8/2B1K3 w - - 0 1");
        Pieza alfil = t.obtenerPieza(Fixtures.p("c1"));
        List<Posicion> destinos = estrategia.movimientos(alfil, Fixtures.p("c1"), t);
        assertTrue(destinos.contains(Fixtures.p("a3")));
        assertTrue(destinos.contains(Fixtures.p("h6")));
    }

    @Test
    public void bordeNoSaleDelTablero() {
        Tablero t = Fixtures.tableroDesdeFen("4k3/8/8/8/8/8/8/B3K3 w - - 0 1");
        Pieza alfil = t.obtenerPieza(Fixtures.p("a1"));
        for (Posicion p : estrategia.movimientos(alfil, Fixtures.p("a1"), t)) {
            assertTrue(t.estaDentro(p));
        }
    }

    @Test
    public void bloqueoConPiezaPropia() {
        Tablero t = Fixtures.tableroDesdeFen("4k3/8/8/8/8/8/1P6/2B1K3 w - - 0 1");
        Pieza alfil = t.obtenerPieza(Fixtures.p("c1"));
        List<Posicion> destinos = estrategia.movimientos(alfil, Fixtures.p("c1"), t);
        assertFalse(destinos.contains(Fixtures.p("b2")));
        assertFalse(destinos.contains(Fixtures.p("a3")));
    }

    @Test
    public void capturaRivalYNoContinuaDetras() {
        Tablero t = Fixtures.tableroDesdeFen("4k3/8/8/8/8/8/1p6/2B1K3 w - - 0 1");
        Pieza alfil = t.obtenerPieza(Fixtures.p("c1"));
        List<Posicion> destinos = estrategia.movimientos(alfil, Fixtures.p("c1"), t);
        assertTrue(destinos.contains(Fixtures.p("b2")));
        assertFalse(destinos.contains(Fixtures.p("a3")));
    }
}
