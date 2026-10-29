package ajedrez.partida;

import ajedrez.modelo.Color;
import ajedrez.modelo.Jugada;
import ajedrez.validacion.MotivoFin;
import java.util.Optional;

public class EnCurso implements EstadoJuego {

    @Override
    public void mover(Juego juego, Jugada jugada) {
        juego.ejecutarJugada(jugada);
    }

    @Override
    public void deshacer(Juego juego) {
        juego.deshacerUltima();
    }

    @Override
    public EstadoJuego siguienteTras(ResultadoJugada resultado) {
        return Transiciones.tras(resultado);
    }

    @Override
    public EstadoJuego abandonar(Color color) {
        return new Terminada(MotivoFin.ABANDONO, color.opuesto());
    }

    @Override
    public EstadoJuego acordarTablas() {
        return new Terminada(MotivoFin.ACUERDO_DE_TABLAS, null);
    }

    @Override
    public EstadoJuego tiempoAgotado(Color perdedor, boolean rivalPuedeGanar) {
        return Transiciones.porTiempo(perdedor, rivalPuedeGanar);
    }

    @Override
    public boolean estaTerminada() {
        return false;
    }

    @Override
    public Optional<Color> getGanador() {
        return Optional.empty();
    }

    @Override
    public Optional<Color> getPerdedor() {
        return Optional.empty();
    }

    @Override
    public boolean esEmpate() {
        return false;
    }

    @Override
    public Optional<MotivoFin> getMotivoFin() {
        return Optional.empty();
    }
}
