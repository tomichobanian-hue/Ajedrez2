package ajedrez.validacion;

import static org.junit.Assert.fail;

import ajedrez.excepciones.MovimientoInvalidoException;
import ajedrez.modelo.Jugada;
import ajedrez.modelo.Tablero;
import ajedrez.soporte.Fixtures;
import org.junit.Test;

public class ReglaMovimientoPermitidoTest {
    private final ReglaMovimientoPermitido regla = new ReglaMovimientoPermitido();

    @Test
    public void destinoEnMovimientosPosiblesPasa() {
        Tablero t = Fixtures.tableroDesdeFen("4k3/8/8/8/8/8/4P3/4K3 w - - 0 1");
        regla.validar(t, new Jugada(Fixtures.p("e2"), Fixtures.p("e3")));
    }

    @Test
    public void destinoNoPermitidoFalla() {
        Tablero t = Fixtures.tableroDesdeFen("4k3/8/8/8/8/8/4P3/4K3 w - - 0 1");
        esperar(() -> regla.validar(t, new Jugada(Fixtures.p("e2"), Fixtures.p("e5"))), "no puede");
    }

    @Test
    public void origenSinPiezaFalla() {
        Tablero t = Fixtures.tableroDesdeFen("4k3/8/8/8/8/8/4P3/4K3 w - - 0 1");
        esperar(() -> regla.validar(t, new Jugada(Fixtures.p("a3"), Fixtures.p("a4"))), "no hay");
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
