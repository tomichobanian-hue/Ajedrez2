package ajedrez.validacion;

import static org.junit.Assert.fail;

import ajedrez.excepciones.MovimientoInvalidoException;
import ajedrez.modelo.Jugada;
import ajedrez.modelo.Posicion;
import ajedrez.soporte.Fixtures;
import org.junit.Test;

public class ReglaPosicionesValidasTest {
    private final ReglaPosicionesValidas regla = new ReglaPosicionesValidas();

    @Test
    public void movimientoDentroDelTableroEsValido() {
        regla.validar(Fixtures.tableroVacio(), new Jugada(Fixtures.p("a1"), Fixtures.p("a2")));
    }

    @Test
    public void origenIgualADestinoFalla() {
        esperar(() -> regla.validar(Fixtures.tableroVacio(), new Jugada(Fixtures.p("e4"), Fixtures.p("e4"))), "misma");
    }

    @Test
    public void origenFueraDelTableroFalla() {
        esperar(() -> regla.validar(Fixtures.tableroVacio(), new Jugada(new Posicion(-1, 0), Fixtures.p("a1"))), "fuera");
    }

    @Test
    public void destinoFueraDelTableroFalla() {
        esperar(() -> regla.validar(Fixtures.tableroVacio(), new Jugada(Fixtures.p("a1"), new Posicion(0, 8))), "fuera");
    }

    @Test
    public void bordesCeroYSieteSonValidos() {
        regla.validar(Fixtures.tableroVacio(), new Jugada(new Posicion(0, 0), new Posicion(7, 7)));
        regla.validar(Fixtures.tableroVacio(), new Jugada(new Posicion(7, 0), new Posicion(0, 7)));
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
