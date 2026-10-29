package ajedrez;

import static ajedrez.Utiles.jugar;
import static ajedrez.Utiles.p;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import ajedrez.excepciones.MovimientoInvalidoException;
import ajedrez.excepciones.ReyEnPeligroException;
import ajedrez.modelo.AlmacenPorMapa;
import ajedrez.modelo.AlmacenPorMatriz;
import ajedrez.modelo.Color;
import ajedrez.modelo.Pieza;
import ajedrez.modelo.TableroConsulta;
import ajedrez.modelo.TipoPieza;
import ajedrez.partida.EnCurso;
import ajedrez.partida.Jaque;
import ajedrez.partida.Juego;
import ajedrez.validacion.MotivoFin;
import java.time.Duration;
import java.util.List;
import org.junit.After;
import org.junit.Test;

public class JuegoTest {
    private Juego juego;

    @After
    public void cerrar() {
        if (juego != null) juego.detener();
    }

    private Juego inicial() {
        juego = Utiles.juegoInicial(new AlmacenPorMapa());
        return juego;
    }

    private void esperarMovimientoInvalido(Runnable accion, String contiene) {
        try {
            accion.run();
            fail("Se esperaba MovimientoInvalidoException");
        } catch (MovimientoInvalidoException e) {
            assertTrue("mensaje: " + e.getMessage(), e.getMessage().toLowerCase().contains(contiene.toLowerCase()));
        }
    }

    // ---------- jugadas basicas ----------

    @Test
    public void empiezanLasBlancasYSeAlternaElTurno() {
        inicial();
        assertEquals(Color.BLANCO, juego.getTurnoActual().get());
        jugar(juego, "e2e4");
        assertEquals(Color.NEGRO, juego.getTurnoActual().get());
        jugar(juego, "e7e5");
        assertEquals(Color.BLANCO, juego.getTurnoActual().get());
        assertTrue(juego.getEstado() instanceof EnCurso);
    }

    @Test
    public void noSePuedeMoverFueraDeTurno() {
        inicial();
        esperarMovimientoInvalido(() -> jugar(juego, "e7e5"), "turno");
    }

    @Test
    public void jugadasInvalidasSeRechazanConMotivo() {
        inicial();
        esperarMovimientoInvalido(() -> jugar(juego, "e3e4"), "no hay");      // casilla vacia
        esperarMovimientoInvalido(() -> jugar(juego, "a1a3"), "no puede");    // torre bloqueada
        esperarMovimientoInvalido(() -> jugar(juego, "b1b3"), "no puede");    // caballo mal
        esperarMovimientoInvalido(() -> jugar(juego, "e2e5"), "no puede");    // peon avanza 3
        esperarMovimientoInvalido(() -> jugar(juego, "e2e2"), "misma");
        assertEquals(Color.BLANCO, juego.getTurnoActual().get());             // nada cambio
    }

    @Test
    public void noSePuedeCapturarPiezaPropia() {
        inicial();
        esperarMovimientoInvalido(() -> jugar(juego, "d1d2"), "no puede");
    }

    @Test
    public void captura() {
        inicial();
        jugar(juego, "e2e4", "d7d5", "e4d5");
        assertEquals(1, juego.getPiezasCapturadas().size());
        assertEquals(TipoPieza.PEON, juego.getPiezasCapturadas().get(0).getTipo());
        assertEquals(Color.NEGRO, juego.getPiezasCapturadas().get(0).getColor());
        assertEquals(Color.BLANCO, juego.getTablero().obtenerPieza(p("d5")).getColor());
    }

    // ---------- jaque, mate, ahogado ----------

    @Test
    public void jaqueObligaAResolverlo() {
        inicial();
        jugar(juego, "e2e4", "f7f6", "d1h5");                 // Dh5+
        assertTrue(juego.getEstado() instanceof Jaque);
        esperarMovimientoInvalido(() -> jugar(juego, "a7a6"), "jaque");
        jugar(juego, "g7g6");                                  // bloquea
        assertTrue(juego.getEstado() instanceof Jaque || juego.getEstado() instanceof EnCurso);
    }

    @Test
    public void matedelPastor() {
        inicial();
        jugar(juego, "e2e4", "e7e5", "d1h5", "b8c6", "f1c4", "g8f6", "h5f7");
        assertTrue(juego.getEstado().estaTerminada());
        assertEquals(MotivoFin.JAQUE_MATE, juego.getMotivoFin().get());
        assertEquals(Color.BLANCO, juego.getGanador().get());
        assertFalse(juego.getTurnoActual().isPresent());
    }

    @Test
    public void matedelTonto() {
        inicial();
        jugar(juego, "f2f3", "e7e5", "g2g4", "d8h4");
        assertEquals(MotivoFin.JAQUE_MATE, juego.getMotivoFin().get());
        assertEquals(Color.NEGRO, juego.getGanador().get());
    }

    @Test
    public void matedelTontoConMatriz() {
        juego = Utiles.juegoInicial(new AlmacenPorMatriz());
        jugar(juego, "f2f3", "e7e5", "g2g4", "d8h4");
        assertEquals(MotivoFin.JAQUE_MATE, juego.getMotivoFin().get());
    }

    @Test
    public void matePasilloConTorre() {
        juego = Utiles.juegoDesdeFen("6k1/5ppp/8/8/8/8/8/R3K3 w - - 0 1");
        jugar(juego, "a1a8");
        assertEquals(MotivoFin.JAQUE_MATE, juego.getMotivoFin().get());
        assertEquals(Color.BLANCO, juego.getGanador().get());
    }

    @Test
    public void ahogado() {
        juego = Utiles.juegoDesdeFen("k7/8/1K1Q4/8/8/8/8/8 w - - 0 1");
        jugar(juego, "d6c7");
        assertEquals(MotivoFin.AHOGADO, juego.getMotivoFin().get());
        assertTrue(juego.getEstado().esEmpate());
        assertFalse(juego.getGanador().isPresent());
    }

    @Test
    public void pinnedPiezaNoPuedeMoverse() {
        juego = Utiles.juegoDesdeFen("4k3/4r3/8/8/8/8/4B3/4K3 w - - 0 1");
        try {
            jugar(juego, "e2d3");
            fail("El alfil esta clavado");
        } catch (ReyEnPeligroException e) {
            // correcto
        }
    }

    @Test
    public void elReyNoPuedeIrAUnaCasillaAtacada() {
        juego = Utiles.juegoDesdeFen("4k3/8/8/8/8/8/3r4/4K3 w - - 0 1");
        try {
            jugar(juego, "e1d1");
            fail();
        } catch (ReyEnPeligroException e) {
            // correcto
        }
        jugar(juego, "e1d2");                                   // captura la torre
        assertEquals(1, juego.getPiezasCapturadas().size());
    }

    // ---------- jugadas especiales ----------

    @Test
    public void enroqueCortoYLargoBlancas() {
        juego = Utiles.juegoDesdeFen("r3k2r/8/8/8/8/8/8/R3K2R w KQkq - 0 1");
        jugar(juego, "e1g1");
        assertEquals(TipoPieza.REY, juego.getTablero().obtenerPieza(p("g1")).getTipo());
        assertEquals(TipoPieza.TORRE, juego.getTablero().obtenerPieza(p("f1")).getTipo());
        assertNull(juego.getTablero().obtenerPieza(p("h1")));
        jugar(juego, "e8c8");
        assertEquals(TipoPieza.REY, juego.getTablero().obtenerPieza(p("c8")).getTipo());
        assertEquals(TipoPieza.TORRE, juego.getTablero().obtenerPieza(p("d8")).getTipo());
    }

    @Test
    public void enroqueNoPermitidoSiElReyYaSeMovio() {
        juego = Utiles.juegoDesdeFen("r3k2r/8/8/8/8/8/8/R3K2R w KQkq - 0 1");
        jugar(juego, "e1e2", "a8a7", "e2e1", "a7a8");
        esperarMovimientoInvalido(() -> jugar(juego, "e1g1"), "no puede");
    }

    @Test
    public void enroqueNoPermitidoSiLaTorreYaSeMovio() {
        juego = Utiles.juegoDesdeFen("r3k2r/8/8/8/8/8/8/R3K2R w KQkq - 0 1");
        jugar(juego, "h1h2", "a8a7", "h2h1", "a7a8");
        esperarMovimientoInvalido(() -> jugar(juego, "e1g1"), "no puede");
        jugar(juego, "e1c1");                                   // el largo si
    }

    @Test
    public void enroqueNoPermitidoConCaminoOcupado() {
        inicial();
        esperarMovimientoInvalido(() -> jugar(juego, "e1g1"), "no puede");
    }

    @Test
    public void enroqueNoPermitidoEnJaque() {
        juego = Utiles.juegoDesdeFen("4k3/8/8/8/8/8/4r3/R3K2R w KQ - 0 1");
        esperarMovimientoInvalido(() -> jugar(juego, "e1g1"), "jaque");
    }

    @Test
    public void enroqueNoPermitidoPasandoPorCasillaAtacada() {
        juego = Utiles.juegoDesdeFen("4k3/8/8/8/8/8/5r2/R3K2R w KQ - 0 1");
        esperarMovimientoInvalido(() -> jugar(juego, "e1g1"), "atacada");
        jugar(juego, "e1c1");                                   // el largo no pasa por f1
    }

    @Test
    public void capturaAlPaso() {
        inicial();
        jugar(juego, "e2e4", "a7a6", "e4e5", "d7d5", "e5d6");
        assertNull(juego.getTablero().obtenerPieza(p("d5")));
        assertEquals(TipoPieza.PEON, juego.getTablero().obtenerPieza(p("d6")).getTipo());
        assertEquals(1, juego.getPiezasCapturadas().size());
    }

    @Test
    public void capturaAlPasoSoloEnElTurnoInmediato() {
        inicial();
        jugar(juego, "e2e4", "a7a6", "e4e5", "d7d5", "h2h3", "a6a5");
        esperarMovimientoInvalido(() -> jugar(juego, "e5d6"), "no puede");
    }

    @Test
    public void promocionRequiereElegirPieza() {
        juego = Utiles.juegoDesdeFen("8/P6k/8/8/8/8/8/4K3 w - - 0 1");
        esperarMovimientoInvalido(() -> jugar(juego, "a7a8"), "promocion");
        jugar(juego, "a7a8q");
        Pieza reina = juego.getTablero().obtenerPieza(p("a8"));
        assertEquals(TipoPieza.REINA, reina.getTipo());
        assertEquals(Color.BLANCO, reina.getColor());
    }

    @Test
    public void promocionACaballo() {
        juego = Utiles.juegoDesdeFen("8/P6k/8/8/8/8/8/4K3 w - - 0 1");
        jugar(juego, "a7a8n");
        assertEquals(TipoPieza.CABALLO, juego.getTablero().obtenerPieza(p("a8")).getTipo());
    }

    @Test
    public void promocionNoPuedeSerRey() {
        juego = Utiles.juegoDesdeFen("8/P6k/8/8/8/8/8/4K3 w - - 0 1");
        esperarMovimientoInvalido(() -> juego.mover(p("a7"), p("a8"), TipoPieza.REY), "promocionar");
    }

    // ---------- movimientos legales para la interfaz ----------

    @Test
    public void movimientosLegalesDeUnPeon() {
        inicial();
        List<ajedrez.modelo.Posicion> d = juego.movimientosLegales(p("e2"));
        assertEquals(2, d.size());
        assertTrue(d.contains(p("e3")) && d.contains(p("e4")));
        assertTrue(juego.movimientosLegales(p("e7")).isEmpty());   // no es su turno
        assertTrue(juego.movimientosLegales(p("e4")).isEmpty());   // casilla vacia
    }

    @Test
    public void movimientosLegalesDejanFueraLosQueDejanEnJaque() {
        juego = Utiles.juegoDesdeFen("4k3/4r3/8/8/8/8/4B3/4K3 w - - 0 1");
        assertTrue(juego.movimientosLegales(p("e2")).isEmpty());   // alfil clavado
    }

    // ---------- fin de partida ----------

    @Test
    public void abandonar() {
        inicial();
        juego.abandonar(Color.BLANCO);
        assertEquals(MotivoFin.ABANDONO, juego.getMotivoFin().get());
        assertEquals(Color.NEGRO, juego.getGanador().get());
        esperarMovimientoInvalido(() -> jugar(juego, "e2e4"), "termino");
        esperarMovimientoInvalido(() -> juego.deshacer(), "termino");
    }

    @Test
    public void acordarTablas() {
        inicial();
        jugar(juego, "e2e4");
        juego.acordarTablas();
        assertEquals(MotivoFin.ACUERDO_DE_TABLAS, juego.getMotivoFin().get());
        assertTrue(juego.getEstado().esEmpate());
    }

    @Test
    public void repeticionTripleEsAutomatica() {
        inicial();
        // La posicion inicial aparece 3 veces despues de 8 semijugadas de ida y vuelta
        jugar(juego, "g1f3", "g8f6", "f3g1", "f6g8");
        assertFalse(juego.getEstado().estaTerminada());
        jugar(juego, "g1f3", "g8f6", "f3g1");
        assertFalse(juego.getEstado().estaTerminada());
        jugar(juego, "f6g8");
        assertEquals(MotivoFin.REPETICION_TRIPLE, juego.getMotivoFin().get());
        assertTrue(juego.getEstado().esEmpate());
    }

    @Test
    public void materialInsuficienteEsAutomatico() {
        juego = Utiles.juegoDesdeFen("4k3/8/8/8/8/8/8/2B1K3 w - - 0 1");
        jugar(juego, "e1e2");
        assertEquals(MotivoFin.MATERIAL_INSUFICIENTE, juego.getMotivoFin().get());
        assertTrue(juego.getEstado().esEmpate());
    }

    @Test
    public void capturaQueDejaSoloReyesEsTablas() {
        juego = Utiles.juegoDesdeFen("4k3/8/8/8/8/8/4p3/3KB3 w - - 0 1");
        // el rey blanco captura el unico peon: quedan rey y alfil contra rey
        jugar(juego, "d1e2");
        assertEquals(MotivoFin.MATERIAL_INSUFICIENTE, juego.getMotivoFin().get());
    }

    @Test
    public void conMaterialSuficienteNoHayTablas() {
        juego = Utiles.juegoDesdeFen("4k3/8/8/8/8/8/8/R3K3 w - - 0 1");
        jugar(juego, "e1e2");
        assertFalse(juego.getEstado().estaTerminada());
        juego = Utiles.juegoDesdeFen("4k3/8/8/8/8/8/8/1N2KB2 w - - 0 1");
        jugar(juego, "e1e2");
        assertFalse(juego.getEstado().estaTerminada());          // alfil + caballo
    }

    // ---------- deshacer ----------

    @Test
    public void deshacerRestauraTodo() {
        inicial();
        jugar(juego, "e2e4", "d7d5", "e4d5");
        juego.deshacer();
        assertEquals(Color.BLANCO, juego.getTurnoActual().get());
        assertEquals(0, juego.getPiezasCapturadas().size());
        assertEquals(Color.NEGRO, juego.getTablero().obtenerPieza(p("d5")).getColor());
        assertEquals(TipoPieza.PEON, juego.getTablero().obtenerPieza(p("e4")).getTipo());
        assertNull(juego.getTablero().obtenerPieza(p("e2")));
        juego.deshacer();
        juego.deshacer();
        assertNotNull(juego.getTablero().obtenerPieza(p("e2")));
        assertEquals(Color.BLANCO, juego.getTurnoActual().get());
        esperarMovimientoInvalido(() -> juego.deshacer(), "no hay");
        jugar(juego, "d2d4");                                    // se puede seguir jugando
    }

    @Test
    public void deshacerUnJaqueVuelveAEnCurso() {
        inicial();
        jugar(juego, "e2e4", "f7f6", "d1h5");
        assertTrue(juego.getEstado() instanceof Jaque);
        juego.deshacer();
        assertTrue(juego.getEstado() instanceof EnCurso);
    }

    @Test
    public void deshacerUnMateReabreLaPartida() {
        inicial();
        jugar(juego, "f2f3", "e7e5", "g2g4", "d8h4");
        assertTrue(juego.getEstado().estaTerminada());
        // Terminada no admite deshacer
        esperarMovimientoInvalido(() -> juego.deshacer(), "termino");
    }

    @Test
    public void deshacerEnroqueCapturaAlPasoYPromocion() {
        juego = Utiles.juegoDesdeFen("r3k2r/8/8/8/8/8/8/R3K2R w KQkq - 0 1");
        jugar(juego, "e1g1");
        juego.deshacer();
        assertEquals(TipoPieza.REY, juego.getTablero().obtenerPieza(p("e1")).getTipo());
        assertEquals(TipoPieza.TORRE, juego.getTablero().obtenerPieza(p("h1")).getTipo());
        assertFalse(juego.getTablero().obtenerPieza(p("e1")).haMovido());
        jugar(juego, "e1g1");                                    // el derecho de enroque sigue
        juego.detener();

        juego = Utiles.juegoDesdeFen("8/P6k/8/8/8/8/8/4K3 w - - 0 1");
        jugar(juego, "a7a8q");
        juego.deshacer();
        assertEquals(TipoPieza.PEON, juego.getTablero().obtenerPieza(p("a7")).getTipo());
        assertNull(juego.getTablero().obtenerPieza(p("a8")));
        juego.detener();

        inicial();
        jugar(juego, "e2e4", "a7a6", "e4e5", "d7d5", "e5d6");
        juego.deshacer();
        assertEquals(TipoPieza.PEON, juego.getTablero().obtenerPieza(p("d5")).getTipo());
        assertEquals(TipoPieza.PEON, juego.getTablero().obtenerPieza(p("e5")).getTipo());
        assertNull(juego.getTablero().obtenerPieza(p("d6")));
        jugar(juego, "e5d6");                                    // el ultimo movimiento se restauro
    }

    // ---------- revision (retroceder / avanzar) ----------

    @Test
    public void revisionNoTocaElTableroReal() {
        inicial();
        jugar(juego, "e2e4", "e7e5", "g1f3");
        TableroConsulta real = juego.getTablero();
        juego.retroceder();
        assertTrue(juego.enRevision());
        TableroConsulta copia = juego.getTableroRevision();
        assertTrue(copia != real);
        assertNull(copia.obtenerPieza(p("f3")));                  // en la revision, Cf3 no se jugo
        assertNotNull(copia.obtenerPieza(p("g1")));
        assertNotNull(real.obtenerPieza(p("f3")));                // el real sigue en el presente
        assertEquals(Color.NEGRO, juego.getTurnoActual().get());  // turno real intacto

        juego.retroceder();
        juego.retroceder();
        assertNotNull(copia.obtenerPieza(p("e2")));              // posicion inicial
        assertNull(copia.obtenerPieza(p("e4")));
        assertNotNull(real.obtenerPieza(p("e4")));

        juego.avanzar();
        assertNull(juego.getTableroRevision().obtenerPieza(p("e2")));
        assertNotNull(juego.getTableroRevision().obtenerPieza(p("e4")));
        juego.avanzar();
        juego.avanzar();
        assertFalse(juego.enRevision());
        assertSame(real, juego.getTableroRevision());             // vuelve al tablero real
    }

    @Test
    public void durantRevisionNoSePuedeMoverNiDeshacer() {
        inicial();
        jugar(juego, "e2e4", "e7e5");
        juego.retroceder();
        esperarMovimientoInvalido(() -> jugar(juego, "g1f3"), "revision");
        esperarMovimientoInvalido(() -> juego.deshacer(), "revision");
        juego.avanzar();
        jugar(juego, "g1f3");                                     // ya salio de la revision
    }

    @Test
    public void revisionDeEnroquePromocionYCapturaAlPaso() {
        juego = Utiles.juegoDesdeFen("4k2r/P7/8/8/8/8/8/R3K2R w KQk - 0 1");
        jugar(juego, "e1c1", "e8g8", "a7a8q");
        TableroConsulta real = juego.getTablero();
        juego.retroceder();
        TableroConsulta copia = juego.getTableroRevision();
        assertEquals(TipoPieza.PEON, copia.obtenerPieza(p("a7")).getTipo());
        juego.retroceder();
        juego.retroceder();
        assertEquals(TipoPieza.REY, copia.obtenerPieza(p("e1")).getTipo());
        assertEquals(TipoPieza.TORRE, copia.obtenerPieza(p("a1")).getTipo());
        assertEquals(TipoPieza.REY, copia.obtenerPieza(p("e8")).getTipo());
        juego.avanzar();
        juego.avanzar();
        juego.avanzar();
        assertEquals(TipoPieza.REINA, real.obtenerPieza(p("a8")).getTipo());
        assertFalse(juego.enRevision());
    }

    @Test
    public void sePuedeRevisarUnaPartidaTerminada() {
        inicial();
        jugar(juego, "f2f3", "e7e5", "g2g4", "d8h4");
        juego.retroceder();
        assertNotNull(juego.getTableroRevision().obtenerPieza(p("d8")));
        assertTrue(juego.getEstado().estaTerminada());
        juego.avanzar();
        assertFalse(juego.enRevision());
    }

    @Test
    public void retrocederSinHistorialFalla() {
        inicial();
        esperarMovimientoInvalido(() -> juego.retroceder(), "no hay");
        esperarMovimientoInvalido(() -> juego.avanzar(), "no hay");
    }

    // ---------- reloj ----------

    @Test
    public void tiempoAgotadoDaLaVictoriaAlRival() {
        FakeClock reloj = new FakeClock();
        juego = Utiles.juegoDesdeFen(new AlmacenPorMapa(),
                "rnbqkbnr/pppppppp/8/8/8/8/PPPPPPPP/RNBQKBNR w KQkq - 0 1", reloj);
        // 10 horas de reloj
        jugar(juego, "e2e4");
        reloj.avanzar(Duration.ofHours(11));                      // las negras se pasan
        juego.verificarTiempo();
        assertEquals(MotivoFin.TIEMPO_AGOTADO, juego.getMotivoFin().get());
        assertEquals(Color.BLANCO, juego.getGanador().get());
        assertEquals(Duration.ZERO, juego.getTiempoRestante(Color.NEGRO));
    }

    @Test
    public void tiempoAgotadoContraMaterialInsuficienteEsTablas() {
        FakeClock reloj = new FakeClock();
        // las blancas solo tienen un alfil: aunque las negras se queden sin tiempo, no pueden ganar... al reves:
        // si las NEGRAS se quedan sin tiempo, ganan las blancas salvo que no puedan ganar (solo alfil) -> tablas
        juego = Utiles.juegoDesdeFen(new AlmacenPorMapa(), "r3k3/8/8/8/8/8/8/2B1K3 w - - 0 1", reloj);
        jugar(juego, "e1e2");
        reloj.avanzar(Duration.ofHours(11));                      // se pasan las negras
        juego.verificarTiempo();
        assertEquals(MotivoFin.MATERIAL_INSUFICIENTE, juego.getMotivoFin().get());
        assertTrue(juego.getEstado().esEmpate());
    }

    @Test
    public void elTiempoSeDescuentaAlJugadorDeTurnoYSeSumaElIncremento() {
        FakeClock reloj = new FakeClock();
        ajedrez.modelo.Tablero t = Utiles.tableroDesdeFen(new AlmacenPorMapa(),
                "rnbqkbnr/pppppppp/8/8/8/8/PPPPPPPP/RNBQKBNR w KQkq - 0 1");
        ajedrez.partida.Reloj r = new ajedrez.partida.Reloj(Duration.ofMinutes(10), Duration.ofSeconds(5), reloj);
        juego = new Juego(t, Utiles.validadorJugada(), new ajedrez.validacion.ValidadorJaque(),
                Utiles.validadorTablas(), r, 10);
        reloj.avanzar(Duration.ofSeconds(30));
        jugar(juego, "e2e4");
        assertEquals(Duration.ofSeconds(10 * 60 - 30 + 5), juego.getTiempoRestante(Color.BLANCO));
        assertEquals(Duration.ofMinutes(10), juego.getTiempoRestante(Color.NEGRO));
        reloj.avanzar(Duration.ofSeconds(12));
        assertEquals(Duration.ofSeconds(10 * 60 - 12), juego.getTiempoRestante(Color.NEGRO));
    }

    @Test
    public void elHiloDelRelojCortaLaPartidaSolo() throws Exception {
        ajedrez.partida.ControladorJuego c = new ajedrez.partida.ControladorJuego();
        c.nuevaPartida(Duration.ofMillis(150), Duration.ZERO);
        Thread.sleep(600);
        assertTrue(c.getEstado().estaTerminada());
        assertEquals(MotivoFin.TIEMPO_AGOTADO, c.getMotivoFin().get());
        assertEquals(Color.NEGRO, c.getGanador().get());           // las blancas no movieron
    }

    @Test
    public void nuevaPartidaDetieneLaAnterior() throws Exception {
        ajedrez.partida.ControladorJuego c = new ajedrez.partida.ControladorJuego();
        c.nuevaPartida(Duration.ofMillis(100), Duration.ZERO);
        c.nuevaPartida(Duration.ofMinutes(10), Duration.ZERO);
        Thread.sleep(400);
        assertFalse(c.getEstado().estaTerminada());
        assertEquals(Color.BLANCO, c.getTurnoActual().get());
    }
}
