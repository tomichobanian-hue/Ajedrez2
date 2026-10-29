package ajedrez.partida;

import ajedrez.modelo.Color;
import ajedrez.modelo.Jugada;
import ajedrez.validacion.MotivoFin;
import java.util.Optional;

/** Estado de la partida. Solo los estados deciden el estado siguiente: devuelven el nuevo. */
public interface EstadoJuego {
    void mover(Juego juego, Jugada jugada);

    void deshacer(Juego juego);

    EstadoJuego siguienteTras(ResultadoJugada resultado);

    EstadoJuego abandonar(Color color);

    EstadoJuego acordarTablas();

    EstadoJuego tiempoAgotado(Color perdedor, boolean rivalPuedeGanar);

    boolean estaTerminada();

    Optional<Color> getGanador();

    Optional<Color> getPerdedor();

    boolean esEmpate();

    Optional<MotivoFin> getMotivoFin();
}
