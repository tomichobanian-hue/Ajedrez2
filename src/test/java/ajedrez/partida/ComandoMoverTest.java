package ajedrez.partida;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import ajedrez.modelo.Color;
import ajedrez.modelo.Jugada;
import ajedrez.modelo.TipoPieza;
import ajedrez.soporte.Fixtures;
import org.junit.After;
import org.junit.Test;

public class ComandoMoverTest {
    private Juego juego;

    @After
    public void cerrar() {
        if (juego != null) juego.detener();
    }

    @Test
    public void ejecutarAplicaLaJugada() {
        juego = Fixtures.juegoInicial();
        ComandoMover comando = new ComandoMover(juego, new Jugada(Fixtures.p("e2"), Fixtures.p("e4")));
        EstadoJuego antes = juego.getEstado();
        comando.ejecutar();
        assertNotNull(comando.getMovimiento());
        assertEquals(TipoPieza.PEON, juego.getTablero().obtenerPieza(Fixtures.p("e4")).getTipo());
        assertEquals(Color.NEGRO, juego.getTurnoActual().get());
        assertTrue(antes instanceof EnCurso);
    }

    @Test
    public void deshacerRevierteYRestauraEstado() {
        juego = Fixtures.juegoDesdeFen("4k3/8/8/8/8/8/4P3/4K3 w - - 0 1");
        ComandoMover comando = new ComandoMover(juego, new Jugada(Fixtures.p("e2"), Fixtures.p("e4")));
        EstadoJuego anterior = juego.getEstado();
        comando.ejecutar();
        comando.deshacer();
        assertEquals(TipoPieza.PEON, juego.getTablero().obtenerPieza(Fixtures.p("e2")).getTipo());
        assertNull(juego.getTablero().obtenerPieza(Fixtures.p("e4")));
        assertEquals(Color.BLANCO, juego.getTurnoActual().get());
        assertEquals(anterior.getClass(), juego.getEstado().getClass());
    }

    @Test
    public void deshacerTrasJaqueRestauraEnCurso() {
        juego = Fixtures.juegoInicial();
        Fixtures.jugar(juego, "e2e4", "f7f6");
        ComandoMover jaque = new ComandoMover(juego, new Jugada(Fixtures.p("d1"), Fixtures.p("h5")));
        jaque.ejecutar();
        assertTrue(juego.getEstado() instanceof Jaque);
        jaque.deshacer();
        assertTrue(juego.getEstado() instanceof EnCurso);
    }
}
