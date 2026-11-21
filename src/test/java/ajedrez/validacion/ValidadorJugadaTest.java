package ajedrez.validacion;

import static org.junit.Assert.fail;

import ajedrez.excepciones.MovimientoInvalidoException;
import ajedrez.modelo.Jugada;
import ajedrez.modelo.Tablero;
import ajedrez.modelo.TableroConsulta;
import ajedrez.modelo.TipoPieza;
import ajedrez.soporte.Fixtures;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import org.junit.Test;

public class ValidadorJugadaTest {

    @Test
    public void todasLasReglasPasan() {
        Tablero t = Fixtures.tableroDesdeFen("4k3/8/8/8/8/8/4P3/4K3 w - - 0 1");
        Fixtures.validadorJugada().validar(t, new Jugada(Fixtures.p("e2"), Fixtures.p("e4")));
    }

    @Test
    public void fallaLaPrimeraRegla() {
        Tablero t = Fixtures.tableroVacio();
        esperar(() -> Fixtures.validadorJugada().validar(t, new Jugada(Fixtures.p("e4"), Fixtures.p("e4"))), "misma");
    }

    @Test
    public void fallaUnaReglaIntermedia() {
        Tablero t = Fixtures.tableroDesdeFen("4k3/8/8/8/8/8/4P3/4K3 w - - 0 1");
        esperar(() -> Fixtures.validadorJugada().validar(t, new Jugada(Fixtures.p("e2"), Fixtures.p("e5"))), "no puede");
    }

    @Test
    public void listaVaciaNoFalla() {
        new ValidadorJugada(List.of()).validar(Fixtures.tableroVacio(), new Jugada(Fixtures.p("a1"), Fixtures.p("a2")));
    }

    @Test
    public void cortaAlFallarLaPrimeraYNoEjecutaLasSiguientes() {
        AtomicInteger llamadas = new AtomicInteger();
        List<ReglaMovimiento> reglas = new ArrayList<>();
        reglas.add((tablero, jugada) -> {
            llamadas.incrementAndGet();
            throw new MovimientoInvalidoException("falla primera");
        });
        reglas.add((tablero, jugada) -> llamadas.incrementAndGet());
        try {
            new ValidadorJugada(reglas).validar(Fixtures.tableroVacio(), new Jugada(Fixtures.p("a1"), Fixtures.p("a2")));
            fail();
        } catch (MovimientoInvalidoException e) {
            org.junit.Assert.assertEquals(1, llamadas.get());
        }
    }

    @Test
    public void promocionInvalidaLaDetectaLaTerceraRegla() {
        Tablero t = Fixtures.tableroDesdeFen("4k3/P7/8/8/8/8/8/4K3 w - - 0 1");
        esperar(() -> Fixtures.validadorJugada().validar(t,
                new Jugada(Fixtures.p("a7"), Fixtures.p("a8"), TipoPieza.REY)), "rey");
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
