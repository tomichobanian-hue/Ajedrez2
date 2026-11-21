package ajedrez.modelo;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNotSame;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import ajedrez.soporte.Fixtures;
import org.junit.Test;

public class TableroTest {

    @Test
    public void moverYDeshacerNormal() {
        Tablero t = Fixtures.tableroDesdeFen("4k3/8/8/8/8/8/4P3/4K3 w - - 0 1");
        Movimiento m = t.mover(new Jugada(Fixtures.p("e2"), Fixtures.p("e4")));
        assertEquals(TipoPieza.PEON, t.obtenerPieza(Fixtures.p("e4")).getTipo());
        assertNull(t.obtenerPieza(Fixtures.p("e2")));
        t.deshacer(m);
        assertEquals(TipoPieza.PEON, t.obtenerPieza(Fixtures.p("e2")).getTipo());
        assertNull(t.obtenerPieza(Fixtures.p("e4")));
    }

    @Test
    public void capturaYDeshacer() {
        Tablero t = Fixtures.tableroDesdeFen("4k3/8/8/8/8/8/p7/R3K3 w - - 0 1");
        Movimiento m = t.mover(new Jugada(Fixtures.p("a1"), Fixtures.p("a2")));
        assertEquals(TipoMovimiento.NORMAL, m.tipo());
        assertNotNull(m.capturada());
        assertNull(t.obtenerPieza(Fixtures.p("a1")));
        t.deshacer(m);
        assertEquals(TipoPieza.TORRE, t.obtenerPieza(Fixtures.p("a1")).getTipo());
        assertEquals(TipoPieza.PEON, t.obtenerPieza(Fixtures.p("a2")).getTipo());
    }

    @Test
    public void enroqueYDeshacer() {
        Tablero t = Fixtures.tableroDesdeFen("4k3/8/8/8/8/8/8/R3K2R w KQ - 0 1");
        Movimiento m = t.mover(new Jugada(Fixtures.p("e1"), Fixtures.p("g1")));
        assertEquals(TipoMovimiento.ENROQUE_CORTO, m.tipo());
        assertEquals(TipoPieza.REY, t.obtenerPieza(Fixtures.p("g1")).getTipo());
        assertEquals(TipoPieza.TORRE, t.obtenerPieza(Fixtures.p("f1")).getTipo());
        t.deshacer(m);
        assertEquals(TipoPieza.REY, t.obtenerPieza(Fixtures.p("e1")).getTipo());
        assertEquals(TipoPieza.TORRE, t.obtenerPieza(Fixtures.p("h1")).getTipo());
        assertFalseHaMovido(t.obtenerPieza(Fixtures.p("e1")));
    }

    @Test
    public void capturaAlPasoYDeshacer() {
        Tablero t = Fixtures.tableroDesdeFen("4k3/3p4/8/4P3/8/8/8/4K3 b - - 0 1");
        t.mover(new Jugada(Fixtures.p("d7"), Fixtures.p("d5")));
        Movimiento m = t.mover(new Jugada(Fixtures.p("e5"), Fixtures.p("d6")));
        assertEquals(TipoMovimiento.CAPTURA_AL_PASO, m.tipo());
        assertNull(t.obtenerPieza(Fixtures.p("d5")));
        assertEquals(TipoPieza.PEON, t.obtenerPieza(Fixtures.p("d6")).getTipo());
        t.deshacer(m);
        assertEquals(TipoPieza.PEON, t.obtenerPieza(Fixtures.p("d5")).getTipo());
        assertEquals(TipoPieza.PEON, t.obtenerPieza(Fixtures.p("e5")).getTipo());
    }

    @Test
    public void promocionYDeshacer() {
        Tablero t = Fixtures.tableroDesdeFen("4k3/P7/8/8/8/8/8/4K3 w - - 0 1");
        Movimiento m = t.mover(new Jugada(Fixtures.p("a7"), Fixtures.p("a8"), TipoPieza.CABALLO));
        assertEquals(TipoMovimiento.PROMOCION, m.tipo());
        assertEquals(TipoPieza.CABALLO, t.obtenerPieza(Fixtures.p("a8")).getTipo());
        t.deshacer(m);
        assertEquals(TipoPieza.PEON, t.obtenerPieza(Fixtures.p("a7")).getTipo());
        assertNull(t.obtenerPieza(Fixtures.p("a8")));
    }

    @Test
    public void copiarEsIndependiente() {
        Tablero t = Fixtures.tableroDesdeFen("4k3/8/8/8/8/8/4P3/4K3 w - - 0 1");
        Tablero copia = t.copiar();
        assertNotSame(t, copia);
        t.mover(new Jugada(Fixtures.p("e2"), Fixtures.p("e4")));
        assertNull(copia.obtenerPieza(Fixtures.p("e4")));
        assertNotNull(copia.obtenerPieza(Fixtures.p("e2")));
    }

    @Test
    public void simularNoModificaElOriginal() {
        Tablero t = Fixtures.tableroDesdeFen("4k3/8/8/8/8/8/4P3/4K3 w - - 0 1");
        TableroConsulta simulado = t.simular(new Jugada(Fixtures.p("e2"), Fixtures.p("e4")));
        assertNotNull(simulado.obtenerPieza(Fixtures.p("e4")));
        assertNull(t.obtenerPieza(Fixtures.p("e4")));
        assertNotNull(t.obtenerPieza(Fixtures.p("e2")));
    }

    @Test
    public void ultimoMovimientoSeRestauraAlDeshacer() {
        Tablero t = Fixtures.tableroDesdeFen("4k3/8/8/8/8/8/4P3/4K3 w - - 0 1");
        assertTrue(t.getUltimoMovimiento().isEmpty());
        Movimiento primero = t.mover(new Jugada(Fixtures.p("e2"), Fixtures.p("e3")));
        Movimiento segundo = t.mover(new Jugada(Fixtures.p("e1"), Fixtures.p("e2")));
        assertEquals(segundo, t.getUltimoMovimiento().get());
        t.deshacer(segundo);
        assertEquals(primero, t.getUltimoMovimiento().get());
        t.deshacer(primero);
        assertTrue(t.getUltimoMovimiento().isEmpty());
    }

    private static void assertFalseHaMovido(Pieza p) {
        org.junit.Assert.assertFalse(p.haMovido());
    }
}
