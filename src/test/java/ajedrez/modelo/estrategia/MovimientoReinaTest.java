package ajedrez.modelo.estrategia;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import ajedrez.modelo.MovimientoDiagonal;
import ajedrez.modelo.MovimientoHorizontalVertical;
import ajedrez.modelo.MovimientoReina;
import ajedrez.modelo.Pieza;
import ajedrez.modelo.Posicion;
import ajedrez.modelo.Tablero;
import ajedrez.soporte.Fixtures;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import org.junit.Test;

public class MovimientoReinaTest {
    private final MovimientoReina estrategia = new MovimientoReina();

    @Test
    public void combinaHorizontalVerticalYDiagonal() {
        Tablero t = Fixtures.tableroDesdeFen("4k3/8/8/8/8/8/8/3QK3 w - - 0 1");
        Pieza reina = t.obtenerPieza(Fixtures.p("d1"));
        List<Posicion> destinos = estrategia.movimientos(reina, Fixtures.p("d1"), t);
        assertTrue(destinos.contains(Fixtures.p("d8"))); // vertical
        assertTrue(destinos.contains(Fixtures.p("a1"))); // horizontal - blocked by king on e1 toward h
        assertTrue(destinos.contains(Fixtures.p("a4"))); // diagonal
        assertTrue(destinos.contains(Fixtures.p("h5"))); // diagonal
    }

    @Test
    public void noGeneraDuplicados() {
        Tablero t = Fixtures.tableroDesdeFen("4k3/8/8/8/8/8/8/3QK3 w - - 0 1");
        Pieza reina = t.obtenerPieza(Fixtures.p("d1"));
        List<Posicion> destinos = estrategia.movimientos(reina, Fixtures.p("d1"), t);
        Set<Posicion> unicos = new HashSet<>(destinos);
        assertEquals(unicos.size(), destinos.size());
    }

    @Test
    public void respetaBloqueosComoSusPartes() {
        Tablero t = Fixtures.tableroDesdeFen("4k3/8/8/8/8/8/3P4/3QK3 w - - 0 1");
        Pieza reina = t.obtenerPieza(Fixtures.p("d1"));
        List<Posicion> destinos = estrategia.movimientos(reina, Fixtures.p("d1"), t);
        assertTrue(!destinos.contains(Fixtures.p("d2")));
        assertTrue(!destinos.contains(Fixtures.p("d3")));
    }

    @Test
    public void capturaCorrectamente() {
        Tablero t = Fixtures.tableroDesdeFen("4k3/8/8/8/8/8/3p4/3QK3 w - - 0 1");
        Pieza reina = t.obtenerPieza(Fixtures.p("d1"));
        assertTrue(estrategia.movimientos(reina, Fixtures.p("d1"), t).contains(Fixtures.p("d2")));
    }

    @Test
    public void equivaleALaUnionDeTorreYAlfil() {
        Tablero t = Fixtures.tableroDesdeFen("4k3/8/8/8/4Q3/8/8/4K3 w - - 0 1");
        Pieza reina = t.obtenerPieza(Fixtures.p("e4"));
        Set<Posicion> reinaSet = new HashSet<>(estrategia.movimientos(reina, Fixtures.p("e4"), t));
        Set<Posicion> union = new HashSet<>();
        union.addAll(new MovimientoHorizontalVertical().movimientos(reina, Fixtures.p("e4"), t));
        union.addAll(new MovimientoDiagonal().movimientos(reina, Fixtures.p("e4"), t));
        assertEquals(union, reinaSet);
    }
}
