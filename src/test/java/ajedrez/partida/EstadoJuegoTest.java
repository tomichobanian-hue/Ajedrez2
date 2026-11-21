package ajedrez.partida;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import ajedrez.excepciones.MovimientoInvalidoException;
import ajedrez.modelo.Color;
import ajedrez.modelo.Jugada;
import ajedrez.soporte.Fixtures;
import ajedrez.soporte.RelojControlable;
import ajedrez.validacion.MotivoFin;
import ajedrez.validacion.ResultadoPosicion;
import java.util.Optional;
import org.junit.After;
import org.junit.Test;

public class EstadoJuegoTest {
    private Juego juego;

    @After
    public void cerrar() {
        if (juego != null) juego.detener();
    }

    @Test
    public void enCursoPermiteMoverYDeshacer() {
        juego = Fixtures.juegoInicial();
        assertTrue(juego.getEstado() instanceof EnCurso);
        Fixtures.jugar(juego, "e2e4");
        juego.deshacer();
        assertNotNullPiezaE2();
    }

    @Test
    public void enCursoPermiteAbandonarYAcordarTablas() {
        juego = Fixtures.juegoInicial();
        juego.abandonar(Color.BLANCO);
        assertTrue(juego.getEstado() instanceof Terminada);
        assertEquals(MotivoFin.ABANDONO, juego.getMotivoFin().get());
        assertEquals(Color.NEGRO, juego.getGanador().get());

        juego.detener();
        juego = Fixtures.juegoInicial();
        juego.acordarTablas();
        assertTrue(juego.getEstado().esEmpate());
        assertEquals(MotivoFin.ACUERDO_DE_TABLAS, juego.getMotivoFin().get());
    }

    @Test
    public void jaqueCambiaElMensajeSiNoSeResuelve() {
        juego = Fixtures.juegoInicial();
        Fixtures.jugar(juego, "e2e4", "f7f6", "d1h5");
        assertTrue(juego.getEstado() instanceof Jaque);
        try {
            Fixtures.jugar(juego, "a7a6");
            fail();
        } catch (MovimientoInvalidoException e) {
            assertTrue(e.getMessage().toLowerCase().contains("jaque"));
        }
    }

    @Test
    public void terminadaRechazaMoverDeshacerAbandonarYAcordar() {
        Terminada terminada = new Terminada(MotivoFin.JAQUE_MATE, Color.BLANCO);
        juego = Fixtures.juegoInicial();
        // Usar el estado directamente sobre un juego recién creado no cambia el de juego;
        // probamos la API del estado con el juego como parámetro.
        try {
            terminada.mover(juego, new Jugada(Fixtures.p("e2"), Fixtures.p("e4")));
            fail();
        } catch (MovimientoInvalidoException e) {
            assertTrue(e.getMessage().toLowerCase().contains("termino"));
        }
        try {
            terminada.deshacer(juego);
            fail();
        } catch (MovimientoInvalidoException e) {
            assertTrue(e.getMessage().toLowerCase().contains("termino"));
        }
        try {
            terminada.abandonar(Color.BLANCO);
            fail();
        } catch (MovimientoInvalidoException e) {
            assertTrue(e.getMessage().toLowerCase().contains("termino"));
        }
        try {
            terminada.acordarTablas();
            fail();
        } catch (MovimientoInvalidoException e) {
            assertTrue(e.getMessage().toLowerCase().contains("termino"));
        }
        assertTrue(terminada.estaTerminada());
        assertEquals(Optional.of(Color.BLANCO), terminada.getGanador());
        assertEquals(Optional.of(MotivoFin.JAQUE_MATE), terminada.getMotivoFin());
        assertFalse(terminada.esEmpate());
    }

    @Test
    public void transicionAJaque() {
        juego = Fixtures.juegoInicial();
        Fixtures.jugar(juego, "e2e4", "f7f6", "d1h5");
        assertTrue(juego.getEstado() instanceof Jaque);
    }

    @Test
    public void transicionAMateConGanador() {
        juego = Fixtures.juegoInicial();
        Fixtures.jugar(juego, "f2f3", "e7e5", "g2g4", "d8h4");
        assertTrue(juego.getEstado() instanceof Terminada);
        assertEquals(MotivoFin.JAQUE_MATE, juego.getMotivoFin().get());
        assertEquals(Color.NEGRO, juego.getGanador().get());
    }

    @Test
    public void transicionAAhogadoSinGanador() {
        juego = Fixtures.juegoDesdeFen("k7/8/1K1Q4/8/8/8/8/8 w - - 0 1");
        Fixtures.jugar(juego, "d6c7");
        assertTrue(juego.getEstado().esEmpate());
        assertEquals(MotivoFin.AHOGADO, juego.getMotivoFin().get());
        assertFalse(juego.getGanador().isPresent());
    }

    @Test
    public void transicionATablasPorMaterial() {
        juego = Fixtures.juegoDesdeFen("4k3/8/8/8/8/8/8/2B1K3 w - - 0 1");
        Fixtures.jugar(juego, "e1e2");
        assertEquals(MotivoFin.MATERIAL_INSUFICIENTE, juego.getMotivoFin().get());
    }

    @Test
    public void tiempoAgotadoConRivalQuePuedeGanar() {
        RelojControlable reloj = new RelojControlable();
        juego = Fixtures.juegoDesdeFen(
                "rnbqkbnr/pppppppp/8/8/8/8/PPPPPPPP/RNBQKBNR w KQkq - 0 1", reloj);
        Fixtures.jugar(juego, "e2e4");
        reloj.avanzar(java.time.Duration.ofHours(11));
        juego.verificarTiempo();
        assertEquals(MotivoFin.TIEMPO_AGOTADO, juego.getMotivoFin().get());
        assertEquals(Color.BLANCO, juego.getGanador().get());
    }

    @Test
    public void tiempoAgotadoConMaterialInsuficienteEsTablas() {
        RelojControlable reloj = new RelojControlable();
        juego = Fixtures.juegoDesdeFen("r3k3/8/8/8/8/8/8/2B1K3 w - - 0 1", reloj);
        Fixtures.jugar(juego, "e1e2");
        reloj.avanzar(java.time.Duration.ofHours(11));
        juego.verificarTiempo();
        assertEquals(MotivoFin.MATERIAL_INSUFICIENTE, juego.getMotivoFin().get());
        assertTrue(juego.getEstado().esEmpate());
    }

    @Test
    public void resultadoJugadaPriorizaMateSobreTablas() {
        ResultadoJugada r = new ResultadoJugada(
                ResultadoPosicion.JAQUE_MATE, Optional.of(MotivoFin.REPETICION_TRIPLE), Color.BLANCO);
        assertEquals(Optional.of(MotivoFin.JAQUE_MATE), r.motivoFinal());
        EstadoJuego siguiente = new EnCurso().siguienteTras(r);
        assertTrue(siguiente instanceof Terminada);
        assertEquals(Optional.of(Color.BLANCO), siguiente.getGanador());
    }

    @Test
    public void resultadoJugadaAhogadoYNormal() {
        assertEquals(Optional.of(MotivoFin.AHOGADO),
                new ResultadoJugada(ResultadoPosicion.AHOGADO, Optional.empty(), Color.BLANCO).motivoFinal());
        assertFalse(new ResultadoJugada(ResultadoPosicion.NORMAL, Optional.empty(), Color.BLANCO).motivoFinal().isPresent());
        assertTrue(new EnCurso().siguienteTras(
                new ResultadoJugada(ResultadoPosicion.NORMAL, Optional.empty(), Color.BLANCO)) instanceof EnCurso);
        assertTrue(new EnCurso().siguienteTras(
                new ResultadoJugada(ResultadoPosicion.JAQUE, Optional.empty(), Color.BLANCO)) instanceof Jaque);
    }

    private void assertNotNullPiezaE2() {
        org.junit.Assert.assertNotNull(juego.getTablero().obtenerPieza(Fixtures.p("e2")));
    }
}
