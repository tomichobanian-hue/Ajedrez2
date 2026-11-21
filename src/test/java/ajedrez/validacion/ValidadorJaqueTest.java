package ajedrez.validacion;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

import ajedrez.excepciones.ReyEnPeligroException;
import ajedrez.modelo.Color;
import ajedrez.modelo.Jugada;
import ajedrez.modelo.Tablero;
import ajedrez.soporte.Fixtures;
import org.junit.Test;

public class ValidadorJaqueTest {
    private final ValidadorJaque validador = new ValidadorJaque();

    @Test
    public void movimientoQueNoDejaAlReyEnJaquePasa() {
        Tablero t = Fixtures.tableroDesdeFen("4k3/8/8/8/8/8/4P3/4K3 w - - 0 1");
        validador.validar(t, new Jugada(Fixtures.p("e2"), Fixtures.p("e4")));
    }

    @Test
    public void piezaClavadaNoPuedeMoverse() {
        Tablero t = Fixtures.tableroDesdeFen("4k3/4r3/8/8/8/8/4B3/4K3 w - - 0 1");
        esperar(() -> validador.validar(t, new Jugada(Fixtures.p("e2"), Fixtures.p("d3"))), "jaque");
    }

    @Test
    public void elReyNoPuedeIrACasillaAtacada() {
        Tablero t = Fixtures.tableroDesdeFen("4k3/8/8/8/8/8/3r4/4K3 w - - 0 1");
        esperar(() -> validador.validar(t, new Jugada(Fixtures.p("e1"), Fixtures.p("d1"))), "jaque");
    }

    @Test
    public void evaluarPosicionDetectaJaque() {
        Tablero t = Fixtures.tableroDesdeFen("4k3/8/8/8/8/8/8/R3K2r w - - 0 1");
        // torre negra en h1 da jaque al rey blanco en e1? path blocked by nothing on rank 1 from h1 to e1 - yes check
        assertEquals(ResultadoPosicion.JAQUE, validador.evaluarPosicion(t, Color.BLANCO));
    }

    @Test
    public void evaluarPosicionDetectaJaqueMate() {
        Tablero t = Fixtures.tableroDesdeFen("6k1/5ppp/8/8/8/8/8/R3K3 w - - 0 1");
        t.mover(new Jugada(Fixtures.p("a1"), Fixtures.p("a8")));
        assertEquals(ResultadoPosicion.JAQUE_MATE, validador.evaluarPosicion(t, Color.NEGRO));
    }

    @Test
    public void evaluarPosicionDetectaAhogado() {
        Tablero t = Fixtures.tableroDesdeFen("k7/8/1K1Q4/8/8/8/8/8 w - - 0 1");
        t.mover(new Jugada(Fixtures.p("d6"), Fixtures.p("c7")));
        assertEquals(ResultadoPosicion.AHOGADO, validador.evaluarPosicion(t, Color.NEGRO));
    }

    @Test
    public void evaluarPosicionNormal() {
        Tablero t = Fixtures.tableroDesdeFen("4k3/8/8/8/8/8/4P3/4K3 w - - 0 1");
        assertEquals(ResultadoPosicion.NORMAL, validador.evaluarPosicion(t, Color.BLANCO));
    }

    @Test
    public void enroqueEstandoEnJaqueFalla() {
        Tablero t = Fixtures.tableroDesdeFen("r3k2r/8/8/8/8/8/8/R3K2R w KQkq - 0 1");
        // torre negra en a8 da jaque? No. Need check on white king.
        // Use: black rook on e8 checking white king on e1 with empty file - but kings on e
        Tablero enJaque = Fixtures.tableroDesdeFen("4r3/8/8/8/8/8/8/R3K2R w KQ - 0 1");
        esperar(() -> validador.validar(enJaque, new Jugada(Fixtures.p("e1"), Fixtures.p("g1"))), "enrocar");
    }

    @Test
    public void enroqueAtravesandoCasillaAtacadaFalla() {
        Tablero t = Fixtures.tableroDesdeFen("4k3/8/8/8/8/8/8/R3K2R w KQ - 0 1");
        // black rook attacks f1
        Tablero atacada = Fixtures.tableroDesdeFen("4k3/8/8/8/8/8/8/R3K1rR w KQ - 0 1");
        // Wait - R on h1 and r on g1 - messy. Better:
        // White king e1, rook h1, black rook on f8 attacking f1 (path through)
        Tablero limpia = Fixtures.tableroDesdeFen("5r2/8/8/8/8/8/8/R3K2R w KQ - 0 1");
        esperar(() -> validador.validar(limpia, new Jugada(Fixtures.p("e1"), Fixtures.p("g1"))), "atacada");
    }

    private static void esperar(Runnable accion, String contiene) {
        try {
            accion.run();
            fail("Se esperaba ReyEnPeligroException");
        } catch (ReyEnPeligroException e) {
            if (!e.getMessage().toLowerCase().contains(contiene.toLowerCase())) {
                fail("mensaje: " + e.getMessage());
            }
        }
    }
}
