package ajedrez.modelo.estrategia;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import ajedrez.modelo.Jugada;
import ajedrez.modelo.MovimientoRey;
import ajedrez.modelo.Pieza;
import ajedrez.modelo.Posicion;
import ajedrez.modelo.Tablero;
import ajedrez.soporte.Fixtures;
import java.util.List;
import org.junit.Test;

public class MovimientoReyTest {
    private final MovimientoRey estrategia = new MovimientoRey();

    @Test
    public void movimientosAdyacentesEnCentro() {
        Tablero t = Fixtures.tableroDesdeFen("4k3/8/8/8/4K3/8/8/8 w - - 0 1");
        Pieza rey = t.obtenerPieza(Fixtures.p("e4"));
        List<Posicion> destinos = estrategia.movimientos(rey, Fixtures.p("e4"), t);
        assertEquals(8, destinos.size());
    }

    @Test
    public void bordeDelTableroReduceOpciones() {
        Tablero t = Fixtures.tableroDesdeFen("4k3/8/8/8/8/8/8/K7 w - - 0 1");
        Pieza rey = t.obtenerPieza(Fixtures.p("a1"));
        assertEquals(3, estrategia.movimientos(rey, Fixtures.p("a1"), t).size());
    }

    @Test
    public void piezaPropiaBloqueaCasilla() {
        Tablero t = Fixtures.tableroDesdeFen("4k3/8/8/8/8/8/1P6/K7 w - - 0 1");
        Pieza rey = t.obtenerPieza(Fixtures.p("a1"));
        assertFalse(estrategia.movimientos(rey, Fixtures.p("a1"), t).contains(Fixtures.p("b2")));
    }

    @Test
    public void capturaRival() {
        Tablero t = Fixtures.tableroDesdeFen("4k3/8/8/8/8/8/1p6/K7 w - - 0 1");
        Pieza rey = t.obtenerPieza(Fixtures.p("a1"));
        assertTrue(estrategia.movimientos(rey, Fixtures.p("a1"), t).contains(Fixtures.p("b2")));
    }

    @Test
    public void casillasAtacadasNoIncluyenEnroque() {
        Tablero t = Fixtures.tableroDesdeFen("4k3/8/8/8/8/8/8/R3K2R w KQ - 0 1");
        Pieza rey = t.obtenerPieza(Fixtures.p("e1"));
        List<Posicion> atacadas = estrategia.casillasAtacadas(rey, Fixtures.p("e1"), t);
        assertFalse(atacadas.contains(Fixtures.p("g1")));
        assertFalse(atacadas.contains(Fixtures.p("c1")));
        assertTrue(estrategia.movimientos(rey, Fixtures.p("e1"), t).contains(Fixtures.p("g1")));
    }

    @Test
    public void enroqueCortoValido() {
        Tablero t = Fixtures.tableroDesdeFen("4k3/8/8/8/8/8/8/R3K2R w KQ - 0 1");
        Pieza rey = t.obtenerPieza(Fixtures.p("e1"));
        assertTrue(estrategia.puedeEnrocar(rey, Fixtures.p("e1"), t, 1));
        assertTrue(estrategia.movimientos(rey, Fixtures.p("e1"), t).contains(Fixtures.p("g1")));
    }

    @Test
    public void enroqueLargoValido() {
        Tablero t = Fixtures.tableroDesdeFen("4k3/8/8/8/8/8/8/R3K2R w KQ - 0 1");
        Pieza rey = t.obtenerPieza(Fixtures.p("e1"));
        assertTrue(estrategia.puedeEnrocar(rey, Fixtures.p("e1"), t, -1));
        assertTrue(estrategia.movimientos(rey, Fixtures.p("e1"), t).contains(Fixtures.p("c1")));
    }

    @Test
    public void reyMovidoNoPuedeEnrocar() {
        Tablero t = Fixtures.tableroDesdeFen("4k3/8/8/8/8/8/8/R3K2R w KQ - 0 1");
        t.mover(new Jugada(Fixtures.p("e1"), Fixtures.p("e2")));
        t.mover(new Jugada(Fixtures.p("e2"), Fixtures.p("e1")));
        Pieza rey = t.obtenerPieza(Fixtures.p("e1"));
        assertFalse(estrategia.puedeEnrocar(rey, Fixtures.p("e1"), t, 1));
    }

    @Test
    public void torreMovidaNoPuedeEnrocar() {
        Tablero t = Fixtures.tableroDesdeFen("4k3/8/8/8/8/8/8/R3K2R w KQ - 0 1");
        t.mover(new Jugada(Fixtures.p("h1"), Fixtures.p("h2")));
        t.mover(new Jugada(Fixtures.p("h2"), Fixtures.p("h1")));
        Pieza rey = t.obtenerPieza(Fixtures.p("e1"));
        assertFalse(estrategia.puedeEnrocar(rey, Fixtures.p("e1"), t, 1));
    }

    @Test
    public void caminoBloqueadoNoPuedeEnrocar() {
        Tablero t = Fixtures.tableroDesdeFen("4k3/8/8/8/8/8/8/R3KB1R w KQ - 0 1");
        Pieza rey = t.obtenerPieza(Fixtures.p("e1"));
        assertFalse(estrategia.puedeEnrocar(rey, Fixtures.p("e1"), t, 1));
    }
}
