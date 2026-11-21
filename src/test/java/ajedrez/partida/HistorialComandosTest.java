package ajedrez.partida;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import ajedrez.excepciones.MovimientoInvalidoException;
import ajedrez.modelo.Jugada;
import ajedrez.modelo.Movimiento;
import ajedrez.modelo.Tablero;
import ajedrez.modelo.TipoMovimiento;
import ajedrez.soporte.Fixtures;
import java.util.concurrent.atomic.AtomicInteger;
import org.junit.After;
import org.junit.Test;

public class HistorialComandosTest {
    private Juego juego;

    @After
    public void cerrar() {
        if (juego != null) juego.detener();
    }

    @Test
    public void ejecutarAgregaComando() {
        Tablero t = Fixtures.tableroVacio();
        HistorialComandos h = new HistorialComandos(t);
        AtomicInteger veces = new AtomicInteger();
        h.ejecutar(comandoStub(veces, movimientoDummy(t)));
        assertEquals(1, veces.get());
        assertEquals(1, h.cantidadHechas());
    }

    @Test
    public void deshacerRevierteUltimo() {
        Tablero t = Fixtures.tableroVacio();
        HistorialComandos h = new HistorialComandos(t);
        AtomicInteger veces = new AtomicInteger();
        h.ejecutar(comandoStub(veces, movimientoDummy(t)));
        h.deshacer();
        assertEquals(0, veces.get());
        assertEquals(0, h.cantidadHechas());
    }

    @Test
    public void deshacerVacioFalla() {
        HistorialComandos h = new HistorialComandos(Fixtures.tableroVacio());
        try {
            h.deshacer();
            fail();
        } catch (MovimientoInvalidoException e) {
            assertTrue(e.getMessage().toLowerCase().contains("no hay"));
        }
    }

    @Test
    public void retrocederYAvanzarSobreCopia() {
        juego = Fixtures.juegoInicial();
        Fixtures.jugar(juego, "e2e4", "e7e5");
        juego.retroceder();
        assertTrue(juego.enRevision());
        assertNull(juego.getTableroRevision().obtenerPieza(Fixtures.p("e5")));
        assertNotNull(juego.getTablero().obtenerPieza(Fixtures.p("e5")));
        juego.avanzar();
        assertFalse(juego.enRevision());
    }

    @Test
    public void ejecutarDuranteRevisionSeBloquea() {
        Tablero t = Fixtures.tableroDesdeFen("4k3/8/8/8/8/8/4P3/4K3 w - - 0 1");
        HistorialComandos h = new HistorialComandos(t);
        Movimiento m = t.mover(new Jugada(Fixtures.p("e2"), Fixtures.p("e4")));
        AtomicInteger veces = new AtomicInteger();
        h.ejecutar(comandoStub(veces, m));
        // Forzar revisión: retroceder necesita el comando en hechos con movimiento
        h.retroceder();
        assertTrue(h.enRevision());
        try {
            h.ejecutar(comandoStub(new AtomicInteger(), m));
            fail();
        } catch (MovimientoInvalidoException e) {
            assertTrue(e.getMessage().toLowerCase().contains("revision"));
        }
    }

    @Test
    public void deshacerDuranteRevisionSeBloquea() {
        Tablero t = Fixtures.tableroDesdeFen("4k3/8/8/8/8/8/4P3/4K3 w - - 0 1");
        HistorialComandos h = new HistorialComandos(t);
        Movimiento m = t.mover(new Jugada(Fixtures.p("e2"), Fixtures.p("e4")));
        h.ejecutar(comandoStub(new AtomicInteger(), m));
        h.retroceder();
        try {
            h.deshacer();
            fail();
        } catch (MovimientoInvalidoException e) {
            assertTrue(e.getMessage().toLowerCase().contains("revision"));
        }
    }

    @Test
    public void alAgotarRehacerSeLimpiaLaRevision() {
        juego = Fixtures.juegoInicial();
        Fixtures.jugar(juego, "e2e4");
        juego.retroceder();
        assertTrue(juego.enRevision());
        juego.avanzar();
        assertFalse(juego.enRevision());
    }

    private static Comando comandoStub(AtomicInteger balance, Movimiento movimiento) {
        return new Comando() {
            @Override
            public void ejecutar() {
                balance.incrementAndGet();
            }

            @Override
            public void deshacer() {
                balance.decrementAndGet();
            }

            @Override
            public Movimiento getMovimiento() {
                return movimiento;
            }
        };
    }

    private static Movimiento movimientoDummy(Tablero t) {
        // Pieza mínima para construir un Movimiento válido para revisión
        var peon = t.crearPieza(ajedrez.modelo.TipoPieza.PEON, ajedrez.modelo.Color.BLANCO);
        return new Movimiento(Fixtures.p("e2"), Fixtures.p("e4"), peon, null, TipoMovimiento.NORMAL,
                null, null, false, false, null);
    }
}
