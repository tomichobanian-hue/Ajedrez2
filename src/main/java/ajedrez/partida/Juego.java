package ajedrez.partida;

import ajedrez.excepciones.MovimientoInvalidoException;
import ajedrez.excepciones.ReyEnPeligroException;
import ajedrez.modelo.Color;
import ajedrez.modelo.Jugada;
import ajedrez.modelo.Movimiento;
import ajedrez.modelo.Pieza;
import ajedrez.modelo.Posicion;
import ajedrez.modelo.Tablero;
import ajedrez.modelo.TableroConsulta;
import ajedrez.modelo.TipoPieza;
import ajedrez.validacion.MotivoFin;
import ajedrez.validacion.ResultadoPosicion;
import ajedrez.validacion.ValidadorJaque;
import ajedrez.validacion.ValidadorMovimiento;
import ajedrez.validacion.ValidadorTablas;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Modelo de la partida. Solo coordina: crea la Jugada, la pasa a los validadores y, si la
 * aprueban, se la entrega al tablero. Los estados deciden el estado siguiente.
 * Los metodos publicos son synchronized porque el HiloReloj llama desde otro hilo.
 */
public final class Juego {
    private static final long PERIODO_POR_DEFECTO_MS = 50;

    private final Tablero tablero;
    private final Turnero turnero = new Turnero();
    private final Reloj reloj;
    private final ValidadorMovimiento validadorJugada;
    private final ValidadorJaque validadorJaque;
    private final ValidadorTablas validadorTablas;
    private final HistorialComandos historial;
    private final List<Pieza> piezasCapturadas = new ArrayList<>();
    private final HiloReloj hiloReloj;
    private EstadoJuego estado = new EnCurso();

    public Juego(Tablero tablero, ValidadorMovimiento validadorJugada, ValidadorJaque validadorJaque,
                 ValidadorTablas validadorTablas, Reloj reloj) {
        this(tablero, validadorJugada, validadorJaque, validadorTablas, reloj, PERIODO_POR_DEFECTO_MS);
    }

    public Juego(Tablero tablero, ValidadorMovimiento validadorJugada, ValidadorJaque validadorJaque,
                 ValidadorTablas validadorTablas, Reloj reloj, long periodoRelojMs) {
        this.tablero = tablero;
        this.validadorJugada = validadorJugada;
        this.validadorJaque = validadorJaque;
        this.validadorTablas = validadorTablas;
        this.reloj = reloj;
        this.historial = new HistorialComandos(tablero);
        this.validadorTablas.iniciar(tablero, Color.BLANCO);
        this.reloj.iniciar();
        this.hiloReloj = new HiloReloj(reloj, this, periodoRelojMs);
        this.hiloReloj.iniciar();
    }

    // ---- acciones ----

    public synchronized void mover(Posicion origen, Posicion destino, TipoPieza promocion) {
        if (historial.enRevision()) {
            throw new MovimientoInvalidoException("Hay una revision en curso: vuelve a la ultima jugada");
        }
        estado.mover(this, new Jugada(origen, destino, promocion));
    }

    public synchronized void deshacer() {
        estado.deshacer(this);
    }

    public synchronized void retroceder() {
        historial.retroceder();
    }

    public synchronized void avanzar() {
        historial.avanzar();
    }

    public synchronized void abandonar(Color color) {
        cambiarEstado(estado.abandonar(color));
    }

    public synchronized void acordarTablas() {
        cambiarEstado(estado.acordarTablas());
    }

    /** Lo llama el HiloReloj: si a alguien se le acabo el tiempo, gana el rival, salvo que no pueda ganar. */
    public synchronized void verificarTiempo() {
        if (estado.estaTerminada()) return;
        Optional<Color> agotado = reloj.tiempoAgotado();
        if (agotado.isEmpty()) return;
        Color perdedor = agotado.get();
        boolean rivalPuedeGanar = validadorTablas.puedeGanar(tablero, perdedor.opuesto());
        cambiarEstado(estado.tiempoAgotado(perdedor, rivalPuedeGanar));
    }

    public synchronized void detener() {
        reloj.detener();
        hiloReloj.detener();
    }

    // ---- consultas ----

    public synchronized EstadoJuego getEstado() {
        return estado;
    }

    public synchronized Optional<Color> getTurnoActual() {
        return estado.estaTerminada() ? Optional.empty() : Optional.of(turnero.getTurnoActual());
    }

    public synchronized TableroConsulta getTablero() {
        return tablero;
    }

    /** La copia que se esta revisando si hay revision en curso; si no, el tablero real. */
    public synchronized TableroConsulta getTableroRevision() {
        TableroConsulta copia = historial.getTableroRevision();
        return copia != null ? copia : tablero;
    }

    public synchronized boolean enRevision() {
        return historial.enRevision();
    }

    public synchronized Duration getTiempoRestante(Color color) {
        return reloj.getRestante(color);
    }

    public synchronized List<Pieza> getPiezasCapturadas() {
        return List.copyOf(piezasCapturadas);
    }

    public synchronized Optional<Color> getGanador() {
        return estado.getGanador();
    }

    public synchronized Optional<MotivoFin> getMotivoFin() {
        return estado.getMotivoFin();
    }

    /** Destinos legales de la pieza en esa casilla (vacio si no es su turno o la partida termino). */
    public synchronized List<Posicion> movimientosLegales(Posicion origen) {
        List<Posicion> legales = new ArrayList<>();
        if (estado.estaTerminada() || historial.enRevision()) return legales;
        Pieza pieza = tablero.obtenerPieza(origen);
        if (pieza == null || !turnero.esTurnoDe(pieza.getColor())) return legales;
        for (Posicion destino : pieza.movimientosPosibles(origen, tablero)) {
            Jugada jugada = new Jugada(origen, destino, promocionPara(pieza, destino));
            try {
                validadorJaque.validar(tablero, jugada);
                legales.add(destino);
            } catch (ReyEnPeligroException e) {
                // no es legal: deja al rey en jaque
            }
        }
        return legales;
    }

    private TipoPieza promocionPara(Pieza pieza, Posicion destino) {
        boolean llegaAlFondo = pieza.getTipo() == TipoPieza.PEON
                && destino.fila() == (pieza.getColor() == Color.BLANCO ? 7 : 0);
        return llegaAlFondo ? TipoPieza.REINA : null;
    }

    // ---- uso interno (paquete): estados y comandos ----

    void ejecutarJugada(Jugada jugada) {
        Pieza pieza = tablero.obtenerPieza(jugada.origen());
        if (pieza == null) {
            throw new MovimientoInvalidoException("No hay ninguna pieza en el origen");
        }
        if (!turnero.esTurnoDe(pieza.getColor())) {
            throw new MovimientoInvalidoException("No es el turno de las " + pieza.getColor());
        }
        validadorJugada.validar(tablero, jugada);
        validadorJaque.validar(tablero, jugada);
        historial.ejecutar(new ComandoMover(this, jugada));
    }

    Movimiento aplicarJugada(Jugada jugada) {
        Movimiento movimiento = tablero.mover(jugada);
        if (movimiento.capturada() != null) piezasCapturadas.add(movimiento.capturada());
        Color quienMovio = movimiento.pieza().getColor();
        reloj.registrarJugada(quienMovio);
        turnero.pasarTurno();
        validadorTablas.validar(tablero, jugada);
        Optional<MotivoFin> tablas = validadorTablas.motivoDeTablas(tablero);
        ResultadoPosicion posicion = validadorJaque.evaluarPosicion(tablero, turnero.getTurnoActual());
        cambiarEstado(estado.siguienteTras(new ResultadoJugada(posicion, tablas, quienMovio)));
        return movimiento;
    }

    void revertirJugada(Movimiento movimiento) {
        tablero.deshacer(movimiento);
        if (movimiento.capturada() != null) piezasCapturadas.remove(piezasCapturadas.size() - 1);
        validadorTablas.deshacer();
        turnero.pasarTurno();
        reloj.volverATurno(turnero.getTurnoActual());
    }

    void restaurarEstado(EstadoJuego anterior) {
        cambiarEstado(anterior);
    }

    void deshacerUltima() {
        historial.deshacer();
    }

    private void cambiarEstado(EstadoJuego nuevo) {
        estado = nuevo;
        if (nuevo.estaTerminada()) detener();
    }
}
