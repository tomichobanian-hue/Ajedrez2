package ajedrez.partida;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;

import ajedrez.ia.JugadorComputadora;
import ajedrez.modelo.Color;
import ajedrez.modelo.Jugada;
import ajedrez.soporte.Fixtures;
import java.util.Optional;
import org.junit.After;
import org.junit.Test;

/** Casos de jugarComputadora no cubiertos por ComputadoraTest (stub feliz ya está allí). */
public class JugarComputadoraTest {
    private Juego juego;

    @After
    public void cerrar() {
        if (juego != null) juego.detener();
    }

    @Test
    public void noMueveSiLaPartidaTermino() {
        juego = Fixtures.juegoInicial();
        juego.abandonar(Color.BLANCO);
        JugadorComputadora stub = (copia, turno) -> Optional.of(new Jugada(Fixtures.p("e2"), Fixtures.p("e4")));
        assertFalse(juego.jugarComputadora(stub));
    }

    @Test
    public void noMueveSiHayRevision() {
        juego = Fixtures.juegoInicial();
        Fixtures.jugar(juego, "e2e4");
        juego.retroceder();
        JugadorComputadora stub = (copia, turno) -> Optional.of(new Jugada(Fixtures.p("d2"), Fixtures.p("d4")));
        assertFalse(juego.jugarComputadora(stub));
    }

    @Test
    public void noMueveSiElStubNoEligeJugada() {
        juego = Fixtures.juegoInicial();
        assertFalse(juego.jugarComputadora((copia, turno) -> Optional.empty()));
        assertEquals(Color.BLANCO, juego.getTurnoActual().get());
    }
}
