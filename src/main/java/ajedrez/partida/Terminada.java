package ajedrez.partida;

import ajedrez.excepciones.MovimientoInvalidoException;
import ajedrez.modelo.Color;
import ajedrez.modelo.Jugada;
import ajedrez.validacion.MotivoFin;
import java.util.Optional;

/** La partida termino. No admite ninguna accion, solo consultas. */
public class Terminada implements EstadoJuego {
    private final MotivoFin motivo;
    private final Color ganador;

    public Terminada(MotivoFin motivo, Color ganador) {
        this.motivo = motivo;
        this.ganador = ganador;
    }

    private MovimientoInvalidoException cerrada() {
        return new MovimientoInvalidoException("La partida ya termino: " + motivo.descripcion());
    }

    @Override
    public void mover(Juego juego, Jugada jugada) {
        throw cerrada();
    }

    @Override
    public void deshacer(Juego juego) {
        throw cerrada();
    }

    @Override
    public EstadoJuego siguienteTras(ResultadoJugada resultado) {
        throw cerrada();
    }

    @Override
    public EstadoJuego abandonar(Color color) {
        throw cerrada();
    }

    @Override
    public EstadoJuego acordarTablas() {
        throw cerrada();
    }

    @Override
    public EstadoJuego tiempoAgotado(Color perdedor, boolean rivalPuedeGanar) {
        return this;
    }

    @Override
    public boolean estaTerminada() {
        return true;
    }

    @Override
    public Optional<Color> getGanador() {
        return Optional.ofNullable(ganador);
    }

    @Override
    public Optional<Color> getPerdedor() {
        return Optional.ofNullable(ganador).map(Color::opuesto);
    }

    @Override
    public boolean esEmpate() {
        return ganador == null;
    }

    @Override
    public Optional<MotivoFin> getMotivoFin() {
        return Optional.of(motivo);
    }
}
