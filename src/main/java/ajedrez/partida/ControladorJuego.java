package ajedrez.partida;

import ajedrez.modelo.AlmacenPorMapa;
import ajedrez.modelo.Color;
import ajedrez.modelo.Pieza;
import ajedrez.modelo.Posicion;
import ajedrez.modelo.Tablero;
import ajedrez.modelo.TableroConsulta;
import ajedrez.modelo.TipoPieza;
import ajedrez.validacion.MotivoFin;
import ajedrez.validacion.ReglaCincuentaMovimientos;
import ajedrez.validacion.ReglaMovimiento;
import ajedrez.validacion.ReglaMovimientoPermitido;
import ajedrez.validacion.ReglaPosicionesValidas;
import ajedrez.validacion.ReglaPromocion;
import ajedrez.validacion.ReglaRepeticionTriple;
import ajedrez.validacion.ValidadorJaque;
import ajedrez.validacion.ValidadorJugada;
import ajedrez.validacion.ValidadorTablas;
import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.Optional;

/** Punto de entrada del nucleo. Guarda unicamente la partida y delega todo en ella. */
public class ControladorJuego {
    private Juego partida;

    public void nuevaPartida(Duration tiempo, Duration incremento) {
        if (partida != null) partida.detener();
        Tablero tablero = new Tablero(new AlmacenPorMapa());
        tablero.colocarPosicionInicial();
        ValidadorJugada validadorJugada = new ValidadorJugada(List.<ReglaMovimiento>of(
                new ReglaPosicionesValidas(), new ReglaMovimientoPermitido(), new ReglaPromocion()));
        ValidadorTablas validadorTablas = new ValidadorTablas(List.of(
                new ReglaCincuentaMovimientos(), new ReglaRepeticionTriple()));
        Reloj reloj = new Reloj(tiempo, incremento, Instant::now);
        partida = new Juego(tablero, validadorJugada, new ValidadorJaque(), validadorTablas, reloj);
    }

    public void mover(Posicion origen, Posicion destino, TipoPieza promocion) {
        partida().mover(origen, destino, promocion);
    }

    public void deshacer() {
        partida().deshacer();
    }

    public void retroceder() {
        partida().retroceder();
    }

    public void avanzar() {
        partida().avanzar();
    }

    public void abandonar(Color color) {
        partida().abandonar(color);
    }

    public void acordarTablas() {
        partida().acordarTablas();
    }

    public EstadoJuego getEstado() {
        return partida().getEstado();
    }

    public Optional<Color> getTurnoActual() {
        return partida().getTurnoActual();
    }

    public TableroConsulta getTablero() {
        return partida().getTablero();
    }

    public TableroConsulta getTableroRevision() {
        return partida().getTableroRevision();
    }

    public boolean enRevision() {
        return partida().enRevision();
    }

    public Duration getTiempoRestante(Color color) {
        return partida().getTiempoRestante(color);
    }

    public List<Pieza> getPiezasCapturadas() {
        return partida().getPiezasCapturadas();
    }

    public Optional<Color> getGanador() {
        return partida().getGanador();
    }

    public Optional<MotivoFin> getMotivoFin() {
        return partida().getMotivoFin();
    }

    public List<Posicion> movimientosLegales(Posicion origen) {
        return partida().movimientosLegales(origen);
    }

    private Juego partida() {
        if (partida == null) throw new IllegalStateException("Todavia no hay una partida: llama a nuevaPartida");
        return partida;
    }
}
