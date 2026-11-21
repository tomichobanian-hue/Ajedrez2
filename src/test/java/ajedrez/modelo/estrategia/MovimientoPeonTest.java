package ajedrez.modelo.estrategia;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import ajedrez.modelo.Color;
import ajedrez.modelo.Jugada;
import ajedrez.modelo.MovimientoPeon;
import ajedrez.modelo.Pieza;
import ajedrez.modelo.Posicion;
import ajedrez.modelo.Tablero;
import ajedrez.modelo.TipoPieza;
import ajedrez.soporte.Fixtures;
import java.util.List;
import org.junit.Test;

public class MovimientoPeonTest {
    private final MovimientoPeon estrategia = new MovimientoPeon();

    @Test
    public void avanceSimpleBlanco() {
        Tablero t = Fixtures.tableroDesdeFen("4k3/8/8/8/8/8/4P3/4K3 w - - 0 1");
        Pieza peon = t.obtenerPieza(Fixtures.p("e2"));
        assertTrue(estrategia.movimientos(peon, Fixtures.p("e2"), t).contains(Fixtures.p("e3")));
    }

    @Test
    public void avanceSimpleNegro() {
        Tablero t = Fixtures.tableroDesdeFen("4k3/4p3/8/8/8/8/8/4K3 b - - 0 1");
        Pieza peon = t.obtenerPieza(Fixtures.p("e7"));
        assertTrue(estrategia.movimientos(peon, Fixtures.p("e7"), t).contains(Fixtures.p("e6")));
    }

    @Test
    public void doblePasoDesdePosicionInicial() {
        Tablero t = Fixtures.tableroDesdeFen("4k3/8/8/8/8/8/4P3/4K3 w - - 0 1");
        Pieza peon = t.obtenerPieza(Fixtures.p("e2"));
        assertTrue(estrategia.movimientos(peon, Fixtures.p("e2"), t).contains(Fixtures.p("e4")));
    }

    @Test
    public void doblePasoFueraDeFilaInicialNoPermitido() {
        Tablero t = Fixtures.tableroDesdeFen("4k3/8/8/8/4P3/8/8/4K3 w - - 0 1");
        Pieza peon = t.obtenerPieza(Fixtures.p("e4"));
        List<Posicion> destinos = estrategia.movimientos(peon, Fixtures.p("e4"), t);
        assertTrue(destinos.contains(Fixtures.p("e5")));
        assertFalse(destinos.contains(Fixtures.p("e6")));
    }

    @Test
    public void peonBloqueadoNoAvanza() {
        Tablero t = Fixtures.tableroDesdeFen("4k3/8/8/8/8/4p3/4P3/4K3 w - - 0 1");
        Pieza peon = t.obtenerPieza(Fixtures.p("e2"));
        assertTrue(estrategia.movimientos(peon, Fixtures.p("e2"), t).isEmpty());
    }

    @Test
    public void capturaDiagonalRival() {
        Tablero t = Fixtures.tableroDesdeFen("4k3/8/8/8/8/3p4/4P3/4K3 w - - 0 1");
        Pieza peon = t.obtenerPieza(Fixtures.p("e2"));
        assertTrue(estrategia.movimientos(peon, Fixtures.p("e2"), t).contains(Fixtures.p("d3")));
    }

    @Test
    public void noCapturaPiezaPropia() {
        Tablero t = Fixtures.tableroDesdeFen("4k3/8/8/8/8/3P4/4P3/4K3 w - - 0 1");
        Pieza peon = t.obtenerPieza(Fixtures.p("e2"));
        assertFalse(estrategia.movimientos(peon, Fixtures.p("e2"), t).contains(Fixtures.p("d3")));
    }

    @Test
    public void casillasAtacadasSonDiagonalesNoElAvance() {
        Tablero t = Fixtures.tableroDesdeFen("4k3/8/8/8/8/8/4P3/4K3 w - - 0 1");
        Pieza peon = t.obtenerPieza(Fixtures.p("e2"));
        List<Posicion> atacadas = estrategia.casillasAtacadas(peon, Fixtures.p("e2"), t);
        assertTrue(atacadas.contains(Fixtures.p("d3")));
        assertTrue(atacadas.contains(Fixtures.p("f3")));
        assertFalse(atacadas.contains(Fixtures.p("e3")));
    }

    @Test
    public void capturaAlPasoValida() {
        // Peón blanco en e5; negro juega d7-d5 y queda disponible al paso en d6.
        Tablero t = Fixtures.tableroDesdeFen("4k3/3p4/8/4P3/8/8/8/4K3 b - - 0 1");
        t.mover(new Jugada(Fixtures.p("d7"), Fixtures.p("d5")));
        Pieza peon = t.obtenerPieza(Fixtures.p("e5"));
        assertTrue(estrategia.movimientos(peon, Fixtures.p("e5"), t).contains(Fixtures.p("d6")));
        assertTrue(estrategia.capturaAlPaso(peon, Fixtures.p("e5"), t).isPresent());
    }

    @Test
    public void capturaAlPasoSoloEnTurnoInmediato() {
        Tablero t = Fixtures.tableroDesdeFen("4k3/3p4/8/4P3/8/8/8/4K3 b - - 0 1");
        t.mover(new Jugada(Fixtures.p("d7"), Fixtures.p("d5")));
        t.mover(new Jugada(Fixtures.p("e1"), Fixtures.p("e2")));
        Pieza peon = t.obtenerPieza(Fixtures.p("e5"));
        assertFalse(estrategia.capturaAlPaso(peon, Fixtures.p("e5"), t).isPresent());
    }
}
