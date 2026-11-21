package ajedrez.validacion;

import static org.junit.Assert.fail;

import ajedrez.excepciones.MovimientoInvalidoException;
import ajedrez.modelo.Jugada;
import ajedrez.modelo.Tablero;
import ajedrez.modelo.TipoPieza;
import ajedrez.soporte.Fixtures;
import org.junit.Test;

public class ReglaPromocionTest {
    private final ReglaPromocion regla = new ReglaPromocion();

    @Test
    public void promocionCorrectaPasa() {
        Tablero t = Fixtures.tableroDesdeFen("4k3/P7/8/8/8/8/8/4K3 w - - 0 1");
        regla.validar(t, new Jugada(Fixtures.p("a7"), Fixtures.p("a8"), TipoPieza.REINA));
    }

    @Test
    public void promocionSinPiezaElegidaFalla() {
        Tablero t = Fixtures.tableroDesdeFen("4k3/P7/8/8/8/8/8/4K3 w - - 0 1");
        esperar(() -> regla.validar(t, new Jugada(Fixtures.p("a7"), Fixtures.p("a8"), null)), "promocion");
    }

    @Test
    public void noSePuedePromocionarARey() {
        Tablero t = Fixtures.tableroDesdeFen("4k3/P7/8/8/8/8/8/4K3 w - - 0 1");
        esperar(() -> regla.validar(t, new Jugada(Fixtures.p("a7"), Fixtures.p("a8"), TipoPieza.REY)), "rey");
    }

    @Test
    public void noSePuedePromocionarAPeon() {
        Tablero t = Fixtures.tableroDesdeFen("4k3/P7/8/8/8/8/8/4K3 w - - 0 1");
        esperar(() -> regla.validar(t, new Jugada(Fixtures.p("a7"), Fixtures.p("a8"), TipoPieza.PEON)), "peon");
    }

    @Test
    public void jugadaNormalSinPromocionNoFalla() {
        Tablero t = Fixtures.tableroDesdeFen("4k3/8/8/8/8/8/P7/4K3 w - - 0 1");
        regla.validar(t, new Jugada(Fixtures.p("a2"), Fixtures.p("a3"), null));
    }

    private static void esperar(Runnable accion, String contiene) {
        try {
            accion.run();
            fail("Se esperaba MovimientoInvalidoException");
        } catch (MovimientoInvalidoException e) {
            if (!e.getMessage().toLowerCase().contains(contiene.toLowerCase())) {
                fail("mensaje: " + e.getMessage());
            }
        }
    }
}
